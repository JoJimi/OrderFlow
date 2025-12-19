package org.example.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.domain.Inventory;
import org.example.inventory.domain.InventoryLog;
import org.example.inventory.dto.response.InventoryResponse;
import org.example.inventory.repository.InventoryBatchInsertRepository;
import org.example.inventory.repository.InventoryLogRepository;
import org.example.inventory.repository.InventoryRepository;
import org.example.shared.dto.OrderEvent;
import org.example.shared.dto.PaymentEvent;
import org.example.shared.dto.ProductEvent;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.inventory.InsufficientStockException;
import org.example.shared.exception.inventory.InventoryNotFoundException;
import org.example.shared.exception.inventory.InventoryReservedCannotDeleteException;
import org.example.shared.type.inventory.InventoryAction;
import org.example.shared.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryEventProducer eventProducer;
    private final InventoryBatchInsertRepository batchInsertRepository;

    /**
     * 상품 생성 시 재고 초기화
     * ProductCreated 이벤트 수신 시 호출
     */
    @Transactional
    public void initializeInventory(ProductEvent event) {
        log.info("재고 초기화 시작 - productId: {}", event.productId());

        // 이미 재고가 존재하는지 확인
        if (inventoryRepository.existsByProductId(event.productId())) {
            log.warn("이미 재고가 존재합니다 - productId: {}", event.productId());
            return;
        }

        int defaultStock = 100;

        // 재고 엔티티 생성
        String inventoryId = IdGenerator.generateInventoryId();
        Inventory inventory = Inventory.builder()
                .inventoryId(inventoryId)
                .productId(event.productId())
                .totalStock(defaultStock)
                .reservedStock(0)
                .availableStock(defaultStock)
                .build();

        inventoryRepository.save(inventory);

        // 재고 로그 기록
        createInventoryLog(inventory, null, defaultStock,
                InventoryAction.INCREASE, "상품 생성으로 인한 재고 초기화");

        log.info("재고 초기화 완료 - productId: {}, inventoryId: {}, stock: {}",
                event.productId(), inventoryId, defaultStock);
    }

    /**
     * 대량 초기화 메서드 - JDBC Batch 사용 (고성능)
     */
    @Transactional
    public void bulkInitializeInventory(int count) {
        log.info("대량 재고 초기화 시작 (JDBC Batch) - count: {}", count);

        long startTime = System.currentTimeMillis();

        int batchSize = 1000;
        int totalBatches = (int) Math.ceil((double) count / batchSize);
        int successfulBatches = 0;
        int failedBatches = 0;
        List<Integer> failedBatchNumbers = new ArrayList<>();

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int startIdx = batchNum * batchSize;
            int endIdx = Math.min(startIdx + batchSize, count);

            try {
                saveBatchWithJdbc(startIdx, endIdx);
                successfulBatches++;

                if ((batchNum + 1) % 10 == 0) {
                    log.info("진행 상황: {} / {} 배치 완료 ({} %)",
                            batchNum + 1, totalBatches,
                            (int)((batchNum + 1) * 100.0 / totalBatches));
                }

            } catch (Exception e) {
                log.error("배치 {} 저장 실패", batchNum + 1, e);
                failedBatches++;
                failedBatchNumbers.add(batchNum + 1);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        int totalCreated = count - (failedBatches * batchSize);
        log.info("대량 재고 초기화 완료 - 총 생성: {} 개, 소요 시간: {} ms ({} 초), " +
                        "성공: {} 배치, 실패: {} 배치",
                totalCreated, processingTime, processingTime / 1000.0,
                successfulBatches, failedBatches);

        if (failedBatches > 0) {
            log.warn("실패한 배치 번호: {}", failedBatchNumbers);
        }
    }

    /**
     * JDBC Batch로 배치 저장 (독립 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatchWithJdbc(int startIdx, int endIdx) {
        List<Inventory> inventories = new ArrayList<>(endIdx - startIdx);
        int defaultStock = 100;

        for (int i = startIdx; i < endIdx; i++) {
            String productId = "SEED-1-" + String.format("%06d", i + 1);

            String inventoryId = IdGenerator.generateInventoryId();
            Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .productId(productId)
                    .totalStock(defaultStock)
                    .reservedStock(0)
                    .availableStock(defaultStock)
                    .build();

            inventories.add(inventory);
        }

        // JDBC Batch Insert 실행
        batchInsertRepository.batchInsert(inventories);
    }

    /**
     * 주문 생성 시 재고 예약
     * OrderCreated 이벤트 수신 시 호출
     */
    @Transactional
    public void reserveInventory(OrderEvent event) {
        log.info("재고 예약 시작 - orderId: {}, items: {}",
                event.orderId(), event.items().size());

        try {
            List<String> productIds = event.items().stream()
                    .map(OrderEvent.OrderItemInfo::productId)
                    .toList();

            // 모든 상품의 재고 조회
            List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);
            Map<String, Inventory> inventoryMap = inventories.stream()
                    .collect(Collectors.toMap(Inventory::getProductId, i -> i));

            // 1단계: 재고 부족 검증
            List<String> insufficientProducts = new ArrayList<>();
            for (OrderEvent.OrderItemInfo item : event.items()) {
                Inventory inventory = inventoryMap.get(item.productId());

                if (inventory == null) {
                    log.error("재고 정보가 존재하지 않습니다 - productId: {}", item.productId());
                    throw new InventoryNotFoundException();
                }

                if (inventory.getAvailableStock() < item.quantity()) {
                    log.warn("재고 부족 - productId: {}, 요청: {}, 사용가능: {}",
                            item.productId(), item.quantity(), inventory.getAvailableStock());
                    insufficientProducts.add(item.productId());
                }
            }

            // 재고 부족한 상품이 있으면 전체 예약 실패
            if (!insufficientProducts.isEmpty()) {
                log.error("재고 부족으로 예약 실패 - orderId: {}, insufficientProducts: {}",
                        event.orderId(), insufficientProducts);

                // 재고 부족 이벤트 발행
                for (OrderEvent.OrderItemInfo item : event.items()) {
                    if (insufficientProducts.contains(item.productId())) {
                        Inventory inventory = inventoryMap.get(item.productId());
                        eventProducer.publishInventoryInsufficientEvent(
                                item.productId(),
                                event.orderId(),
                                item.quantity(),
                                inventory.getAvailableStock()
                        );
                    }
                }
                return;
            }

            // 2단계: 모든 상품 재고 예약
            for (OrderEvent.OrderItemInfo item : event.items()) {
                Inventory inventory = inventoryMap.get(item.productId());
                inventory.reserveStock(item.quantity());
                inventoryRepository.save(inventory);

                // 재고 로그 기록
                createInventoryLog(inventory, event.orderId(), item.quantity(),
                        InventoryAction.RESERVE, "주문 생성으로 인한 재고 예약");

                // 재고 예약 이벤트 발행
                eventProducer.publishInventoryReservedEvent(
                        inventory.getInventoryId(),
                        item.productId(),
                        event.orderId(),
                        item.quantity()
                );

                log.info("재고 예약 완료 - productId: {}, quantity: {}, remainingStock: {}",
                        item.productId(), item.quantity(), inventory.getAvailableStock());
            }

            log.info("전체 재고 예약 완료 - orderId: {}", event.orderId());

        } catch (InsufficientStockException e) {
            log.error("재고 예약 실패 - orderId: {}", event.orderId(), e);
            throw e;
        } catch (Exception e) {
            log.error("재고 예약 중 예상치 못한 오류 - orderId: {}", event.orderId(), e);
            throw new RuntimeException("재고 예약 처리 중 오류 발생", e);
        }
    }

    /**
     * 결제 완료 시 재고 차감
     * PaymentCompleted 이벤트 수신 시 호출
     */
    @Transactional
    public void deductInventory(PaymentEvent event) {
        log.info("재고 차감 시작 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        try {
            // 1. orderId로 RESERVE 액션의 로그 조회 (예약된 재고 정보)
            List<InventoryLog> reservedLogs = inventoryLogRepository
                    .findByOrderIdAndAction(event.orderId(), InventoryAction.RESERVE);

            if (reservedLogs.isEmpty()) {
                log.warn("예약된 재고 로그가 없습니다 - orderId: {}", event.orderId());
                return;
            }

            // 2. 각 상품별로 재고 차감
            for (InventoryLog reservedLog : reservedLogs) {
                String productId = reservedLog.getProductId();
                Integer quantity = reservedLog.getQuantity();

                Inventory inventory = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> {
                            log.error("재고 정보가 존재하지 않습니다 - productId: {}", productId);
                            return new InventoryNotFoundException();
                        });

                // 재고 차감 (예약 → 실제 차감)
                inventory.deductStock(quantity);
                inventoryRepository.save(inventory);

                // 재고 로그 기록
                createInventoryLog(inventory, event.orderId(), quantity,
                        InventoryAction.DEDUCT, "결제 완료로 인한 재고 차감");

                // 재고 차감 이벤트 발행
                eventProducer.publishInventoryDeductedEvent(
                        inventory.getInventoryId(),
                        productId,
                        event.orderId(),
                        quantity
                );

                log.info("재고 차감 완료 - productId: {}, quantity: {}, remainingTotal: {}",
                        productId, quantity, inventory.getTotalStock());
            }

            log.info("전체 재고 차감 완료 - orderId: {}, items: {}",
                    event.orderId(), reservedLogs.size());

        } catch (Exception e) {
            log.error("재고 차감 중 오류 발생 - orderId: {}", event.orderId(), e);
            throw new RuntimeException("재고 차감 처리 중 오류 발생", e);
        }
    }

    /**
     * 결제 실패 시 재고 복구
     * PaymentFailed 이벤트 수신 시 호출
     */
    @Transactional
    public void restoreInventory(PaymentEvent event) {
        log.info("재고 복구 시작 - orderId: {}, paymentId: {}, reason: {}",
                event.orderId(), event.paymentId(), event.failureReason());

        try {
            // 1. orderId로 RESERVE 액션의 로그 조회 (예약된 재고 정보)
            List<InventoryLog> reservedLogs = inventoryLogRepository
                    .findByOrderIdAndAction(event.orderId(), InventoryAction.RESERVE);

            if (reservedLogs.isEmpty()) {
                log.warn("예약된 재고 로그가 없습니다 - orderId: {}", event.orderId());
                return;
            }

            // 2. 각 상품별로 재고 복구
            for (InventoryLog reservedLog : reservedLogs) {
                String productId = reservedLog.getProductId();
                Integer quantity = reservedLog.getQuantity();

                Inventory inventory = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> {
                            log.error("재고 정보가 존재하지 않습니다 - productId: {}", productId);
                            return new InventoryNotFoundException();
                        });

                // 재고 복구 (예약 → 사용가능)
                inventory.restoreStock(quantity);
                inventoryRepository.save(inventory);

                // 재고 로그 기록
                createInventoryLog(inventory, event.orderId(), quantity,
                        InventoryAction.RESTORE,
                        String.format("결제 실패로 인한 재고 복구 - 사유: %s", event.failureReason()));

                // 재고 복구 이벤트 발행
                eventProducer.publishInventoryRestoredEvent(
                        inventory.getInventoryId(),
                        productId,
                        event.orderId(),
                        quantity
                );

                log.info("재고 복구 완료 - productId: {}, quantity: {}, availableStock: {}",
                        productId, quantity, inventory.getAvailableStock());
            }

            log.info("전체 재고 복구 완료 - orderId: {}, items: {}",
                    event.orderId(), reservedLogs.size());

        } catch (Exception e) {
            log.error("재고 복구 중 오류 발생 - orderId: {}", event.orderId(), e);
            throw new RuntimeException("재고 복구 처리 중 오류 발생", e);
        }
    }

    /**
     * 상품별 재고 조회
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(String productId) {
        log.info("재고 조회 - productId: {}", productId);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(InventoryNotFoundException::new);

        return InventoryResponse.from(inventory);
    }

    /**
     * 주문의 예약된 재고 로그 조회
     */
    @Transactional(readOnly = true)
    public List<InventoryLog> findReservedLogs(String orderId) {
        return inventoryLogRepository.findByOrderIdAndAction(orderId, InventoryAction.RESERVE);
    }

    /**
     * 상품별 재고 복구 (주문 취소용)
     */
    @Transactional
    public void restoreInventoryByProductId(String productId, String orderId,
                                            Integer quantity, String reason) {
        log.info("재고 복구 - productId: {}, orderId: {}, quantity: {}",
                productId, orderId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("재고 정보가 존재하지 않습니다 - productId: {}", productId);
                    return new InventoryNotFoundException();
                });

        // 재고 복구
        inventory.restoreStock(quantity);
        inventoryRepository.save(inventory);

        // 재고 로그 기록
        createInventoryLog(inventory, orderId, quantity, InventoryAction.RESTORE, reason);

        // 재고 복구 이벤트 발행
        eventProducer.publishInventoryRestoredEvent(
                inventory.getInventoryId(),
                productId,
                orderId,
                quantity
        );

        log.info("재고 복구 완료 - productId: {}, availableStock: {}",
                productId, inventory.getAvailableStock());
    }

    /**
     * 상품 삭제 시 재고 논리 삭제
     * ProductDeleted 이벤트 수신 시 호출
     */
    @Transactional
    public void markInventoryAsDeleted(String productId, String reason) {
        log.info("재고 논리 삭제 시작 - productId: {}", productId);

        try {
            // 1. 재고 조회
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> {
                        log.warn("재고 정보가 존재하지 않습니다 - productId: {}", productId);
                        return new InventoryNotFoundException();
                    });

            // 2. 예약된 재고 확인
            if (inventory.getReservedStock() > 0) {
                log.error("예약된 재고가 있어 삭제 불가 - productId: {}, reservedStock: {}, availableStock: {}",
                        productId, inventory.getReservedStock(), inventory.getAvailableStock());

                throw new InventoryReservedCannotDeleteException();
            }

            // 3. 논리 삭제
            int totalStock = inventory.getTotalStock();
            inventory.markAsDeleted();
            inventoryRepository.save(inventory);

            // 4. 재고 로그 기록
            createInventoryLog(
                    inventory,
                    null,
                    totalStock,
                    InventoryAction.DEDUCT,
                    reason
            );

            log.info("재고 논리 삭제 완료 - productId: {}, inventoryId: {}, 기존재고: {}",
                    productId, inventory.getInventoryId(), totalStock);

        } catch (InventoryNotFoundException e) {
            log.warn("재고 정보가 존재하지 않아 삭제를 건너뜁니다 - productId: {}", productId);
            throw e;

        } catch (BusinessException e) {
            log.error("재고 삭제 중 비즈니스 오류 - productId: {}, error: {}",
                    productId, e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("재고 논리 삭제 중 예상치 못한 오류 - productId: {}", productId, e);
            throw new RuntimeException("재고 논리 삭제 처리 중 오류 발생", e);
        }
    }

    /**
     * 재고 로그 생성
     */
    private void createInventoryLog(Inventory inventory, String orderId, Integer quantity,
                                    InventoryAction action, String reason) {
        String logId = IdGenerator.generateInventoryLogId();

        InventoryLog inventoryLog = InventoryLog.builder()
                .logId(logId)
                .productId(inventory.getProductId())
                .action(action)
                .quantity(quantity)
                .reason(reason)
                .orderId(orderId)
                .build();

        inventory.addInventoryLog(inventoryLog);

        log.info("재고 로그 생성 완료 - logId: {}, action: {}, quantity: {}",
                logId, action, quantity);
    }
}