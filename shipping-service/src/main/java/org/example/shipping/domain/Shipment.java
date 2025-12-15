package org.example.shipping.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.shipping.ShippingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_order_id", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_order_id", columnList = "order_id"),
                @Index(name = "idx_shipping_status", columnList = "shipping_status"),
                @Index(name = "idx_tracking_number", columnList = "tracking_number")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Shipment extends BaseEntity {

    @Id
    @Column(name = "shipment_id", nullable = false, length = 50)
    private String shipmentId;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", nullable = false, length = 30)
    @Builder.Default
    private ShippingStatus shippingStatus = ShippingStatus.SHIPPING_PREPARING;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "carrier", length = 50)
    private String carrier;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    public void startShipping(String trackingNumber, String carrier, LocalDate estimatedDeliveryDate) {
        if (this.shippingStatus != ShippingStatus.SHIPPING_PREPARING) {
            throw new IllegalStateException(
                    String.format("배송 상태가 %s일 때는 시작할 수 없습니다.", this.shippingStatus)
            );
        }
        this.shippingStatus = ShippingStatus.SHIPPING_STARTED;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public void completeShipping() {
        if (this.shippingStatus != ShippingStatus.SHIPPING_STARTED) {
            throw new IllegalStateException(
                    String.format("배송 상태가 %s일 때는 완료할 수 없습니다.", this.shippingStatus)
            );
        }
        this.shippingStatus = ShippingStatus.SHIPPING_COMPLETED;
        this.actualDeliveryDate = LocalDateTime.now();
    }

    public void updateTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("운송장 번호는 필수입니다.");
        }
        this.trackingNumber = trackingNumber;
    }
}