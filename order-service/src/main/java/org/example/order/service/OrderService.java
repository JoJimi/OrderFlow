package org.example.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.Order;
import org.example.order.domain.OrderItem;
import org.example.order.dto.request.OrderCreateRequest;
import org.example.order.dto.response.OrderResponse;
import org.example.order.repository.OrderRepository;
import org.example.shared.dto.OrderEvent;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;
import org.example.shared.exception.order.EmptyOrderItemsException;
import org.example.shared.exception.order.OrderAccessDeniedException;
import org.example.shared.type.OrderStatus;
import org.example.shared.util.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    // TODO: ProductService와 통신하여 상품 정보 및 가격 조회 (향후 FeignClient 사용)

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(String userId, OrderCreateRequest request) {
        log.info("주문 생성 시작 - 사용자 ID: {}, 항목 수: {}", userId, request.items().size());

        // 1. 입력값 검증
        validateOrderRequest(request);

        // 2. 주문 ID 생성
        String orderId = IdGenerator.generateOrderId();

        // 3. 주문 항목 생성
        List<OrderItem> orderItems = createOrderItems(orderId, request.items());

        // 4. 총액 계산
        BigDecimal totalPrice = calculateTotalPrice(orderItems);

        // 5. 주문 엔티티 생성
        Order order = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.ORDER_CREATED)
                .shippingAddress(request.shippingAddress())
                .shippingCity(request.shippingCity())
                .shippingPostalCode(request.shippingPostalCode())
                .build();

        // 6. 주문 항목 추가
        orderItems.forEach(order::addOrderItem);

        // 7. 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 8. Kafka 이벤트 발행
        publishOrderCreatedEvent(savedOrder);

        log.info("주문 생성 완료 - 주문 ID: {}, 총액: {}", orderId, totalPrice);

        return OrderResponse.from(savedOrder);
    }

    /**
     * 사용자별 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String userId, Pageable pageable) {
        log.info("사용자 주문 목록 조회 - 사용자 ID: {}, 페이지: {}", userId, pageable.getPageNumber());
        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponse::from);
    }

    /**
     * 전체 주문 목록 조회 (관리자)
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("전체 주문 목록 조회 - 페이지: {}", pageable.getPageNumber());
        return orderRepository.findAll(pageable)
                .map(OrderResponse::from);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId, String userId) {
        log.info("주문 상세 조회 - 주문 ID: {}, 사용자 ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUserId().equals(userId)) {
            throw new OrderAccessDeniedException();
        }

        return OrderResponse.from(order);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(String orderId, String userId) {
        log.info("주문 취소 - 주문 ID: {}, 사용자 ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUserId().equals(userId)) {
            throw new OrderAccessDeniedException();
        }

        // 주문 취소 (도메인 로직에서 상태 검증)
        order.cancel();

        orderRepository.save(order);

        // Kafka 이벤트 발행 (보상 트랜잭션)
        publishOrderCancelledEvent(order);

        log.info("주문 취소 완료 - 주문 ID: {}", orderId);
    }

    /**
     * 주문 요청 검증
     */
    private void validateOrderRequest(OrderCreateRequest request) {
        if (request.items().isEmpty()) {
            throw new EmptyOrderItemsException();
        }

        // TODO: 상품 존재 여부 및 재고 확인 (ProductService와 통신)
        for (OrderCreateRequest.OrderItemRequest item : request.items()) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
            }
        }
    }

    /**
     * 주문 항목 생성
     */
    private List<OrderItem> createOrderItems(String orderId, List<OrderCreateRequest.OrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(item -> {
                    // TODO: ProductService에서 실제 상품 정보 조회
                    BigDecimal unitPrice = BigDecimal.valueOf(10000); // 임시 가격

                    String orderItemId = IdGenerator.generateOrderItemId();

                    OrderItem orderItem = OrderItem.builder()
                            .orderItemId(orderItemId)
                            .productId(item.productId())
                            .quantity(item.quantity())
                            .unitPrice(unitPrice)
                            .subtotal(BigDecimal.ZERO)
                            .build();

                    orderItem.calculateSubtotal();
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * 총액 계산
     */
    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 주문 생성 이벤트 발행
     */
    private void publishOrderCreatedEvent(Order order) {
        List<OrderEvent.OrderItemInfo> items = order.getOrderItems().stream()
                .map(item -> new OrderEvent.OrderItemInfo(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        OrderEvent.ShippingInfo shippingInfo = new OrderEvent.ShippingInfo(
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingPostalCode()
        );

        OrderEvent event = OrderEvent.created(
                order.getOrderId(),
                order.getUserId(),
                order.getTotalPrice(),
                items,
                shippingInfo
        );

        eventProducer.publishOrderCreatedEvent(event);
    }

    /**
     * 주문 취소 이벤트 발행
     */
    private void publishOrderCancelledEvent(Order order) {
        OrderEvent event = OrderEvent.cancelled(
                order.getOrderId(),
                order.getUserId()
        );

        eventProducer.publishOrderCancelledEvent(event);
    }
}