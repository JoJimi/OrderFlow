package org.example.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.shared.type.OrderEventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 주문 이벤트 DTO (Kafka 메시지)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderEvent(
        String eventId,
        OrderEventType eventType,
        String orderId,
        String userId,
        BigDecimal totalPrice,
        List<OrderItemInfo> items,
        ShippingInfo shippingInfo,
        LocalDateTime eventTimestamp
) {
    /**
     * 주문 항목 정보
     */
    public record OrderItemInfo(
            String productId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    /**
     * 배송 정보
     */
    public record ShippingInfo(
            String address,
            String city,
            String postalCode
    ) {}

    /**
     * 주문 생성 이벤트
     */
    public static OrderEvent created(String orderId, String userId, BigDecimal totalPrice,
                                     List<OrderItemInfo> items, ShippingInfo shippingInfo) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                OrderEventType.ORDER_CREATED,  // ✅ Enum 사용
                orderId,
                userId,
                totalPrice,
                items,
                shippingInfo,
                LocalDateTime.now()
        );
    }

    /**
     * 주문 취소 이벤트
     */
    public static OrderEvent cancelled(String orderId, String userId) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                OrderEventType.ORDER_CANCELLED,  // ✅ Enum 사용
                orderId,
                userId,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 주문 상태 변경 이벤트
     */
    public static OrderEvent statusChanged(String orderId, String userId, String newStatus) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                OrderEventType.ORDER_STATUS_CHANGED,  // ✅ Enum 사용
                orderId,
                userId,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }
}