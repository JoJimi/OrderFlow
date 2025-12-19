package org.example.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.order.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order extends BaseEntity {

    @Id
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDER_CREATED;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void updateStatus(OrderStatus newStatus) {
        if (!this.orderStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다.",
                            this.orderStatus, newStatus)
            );
        }
        this.orderStatus = newStatus;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isCancellable() {
        return (this.orderStatus == OrderStatus.ORDER_CREATED || this.orderStatus == OrderStatus.PAYMENT_PENDING);
    }

    public void cancel() {
        if (!isCancellable()) {
            throw new IllegalStateException(
                    String.format("주문 상태가 %s일 때는 취소할 수 없습니다.", this.orderStatus)
            );
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }
}