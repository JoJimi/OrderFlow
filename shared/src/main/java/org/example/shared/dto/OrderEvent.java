package org.example.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.shared.type.order.OrderEventType;
import org.example.shared.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public record OrderItemInfo(
            String productId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    public record ShippingInfo(
            String address,
            String city,
            String postalCode
    ) {}

    public static OrderEvent created(String orderId, String userId, BigDecimal totalPrice,
                                     List<OrderItemInfo> items, ShippingInfo shippingInfo) {
        return new OrderEvent(
                IdGenerator.generateEventId(),
                OrderEventType.ORDER_CREATED,
                orderId,
                userId,
                totalPrice,
                items,
                shippingInfo,
                LocalDateTime.now()
        );
    }

    public static OrderEvent cancelled(String orderId, String userId) {
        return new OrderEvent(
                IdGenerator.generateEventId(),
                OrderEventType.ORDER_CANCELLED,
                orderId,
                userId,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }
}