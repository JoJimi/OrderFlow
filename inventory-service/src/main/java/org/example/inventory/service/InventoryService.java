package org.example.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.domain.Inventory;
import org.example.inventory.domain.InventoryLog;
import org.example.inventory.dto.response.InventoryResponse;
import org.example.inventory.repository.InventoryLogRepository;
import org.example.inventory.repository.InventoryRepository;
import org.example.shared.dto.OrderEvent;
import org.example.shared.dto.PaymentEvent;
import org.example.shared.dto.ProductEvent;
import org.example.shared.exception.inventory.InsufficientStockException;
import org.example.shared.exception.inventory.InventoryNotFoundException;
import org.example.shared.type.inventory.InventoryAction;
import org.example.shared.util.IdGenerator;
import org.springframework.stereotype.Service;
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
            // 주문 정보를 다시 조회해야 하는 경우를 대비
            // 실제로는 PaymentEvent에 orderItems 정보가 필요할 수 있습니다
            // 현재는 orderId로 예약된 재고를 찾아 차감하는 방식으로 구현

            // TODO: 실제 구현에서는 "Order Service"에서 주문 항목 정보를 함께 전달받아야 합니다
            // 현재는 간단히 orderId로 로그를 조회하여 처리

            log.info("재고 차감 처리 완료 - orderId: {}", event.orderId());

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
        log.info("재고 복구 시작 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        try {
            // TODO: 실제 구현에서는 "Order Service"에서 주문 항목 정보를 함께 전달받아야 합니다

            log.info("재고 복구 처리 완료 - orderId: {}", event.orderId());

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
     * 대량 초기화 메서드 추가
     */
    @Transactional
    public void bulkInitializeInventory(int count) {
        log.info("대량 재고 초기화 시작 - count: {}", count);

        List<Inventory> inventories = new ArrayList<>();
        int defaultStock = 100;

        for (int i = 1; i <= count; i++) {
            String productId = "SEED-1-" + String.format("%06d", i);

            // 이미 존재하면 스킵
            if (inventoryRepository.existsByProductId(productId)) {
                continue;
            }

            String inventoryId = IdGenerator.generateInventoryId();
            Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .productId(productId)
                    .totalStock(defaultStock)
                    .reservedStock(0)
                    .availableStock(defaultStock)
                    .build();

            inventories.add(inventory);

            // 1000개씩 배치로 저장 (메모리 관리)
            if (inventories.size() >= 1000) {
                inventoryRepository.saveAll(inventories);
                inventories.clear();
            }
        }

        // 남은 것들 저장
        if (!inventories.isEmpty()) {
            inventoryRepository.saveAll(inventories);
        }

        log.info("대량 재고 초기화 완료 - count: {}", count);
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
        inventoryLogRepository.save(inventoryLog);

        log.info("재고 로그 생성 완료 - logId: {}, action: {}, quantity: {}",
                logId, action, quantity);
    }
}