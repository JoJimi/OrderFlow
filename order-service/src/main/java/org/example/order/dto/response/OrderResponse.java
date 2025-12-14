package org.example.order.dto.response;

import org.example.order.domain.Order;
import org.example.order.domain.OrderItem;
import org.example.shared.type.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
        String orderId,
        String userId,
        BigDecimal totalPrice,
        OrderStatus orderStatus,
        String shippingAddress,
        String shippingCity,
        String shippingPostalCode,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record OrderItemResponse(
            String orderItemId,
            String productId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getOrderItemId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            );
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingPostalCode(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}