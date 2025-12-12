package org.example.shared.dto;

import org.example.shared.type.ShippingEventType;
import org.example.shared.type.ShippingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 배송 이벤트 DTO
 * Kafka 메시지로 전송되는 배송 상태 변경 이벤트
 */
public record ShippingEvent(
        String shipmentId,
        String orderId,
        String userId,
        ShippingStatus shippingStatus,
        ShippingEventType eventType,
        String trackingNumber,
        String carrier,
        LocalDate estimatedDeliveryDate,
        LocalDateTime actualDeliveryDate,
        LocalDateTime timestamp
) {
    /**
     * 배송 준비 이벤트 생성
     */
    public static ShippingEvent preparing(
            String shipmentId,
            String orderId,
            String userId
    ) {
        return new ShippingEvent(
                shipmentId,
                orderId,
                userId,
                ShippingStatus.SHIPPING_PREPARING,
                ShippingEventType.SHIPPING_PREPARING,
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 배송 시작 이벤트 생성
     */
    public static ShippingEvent started(
            String shipmentId,
            String orderId,
            String userId,
            String trackingNumber,
            String carrier,
            LocalDate estimatedDeliveryDate
    ) {
        return new ShippingEvent(
                shipmentId,
                orderId,
                userId,
                ShippingStatus.SHIPPING_STARTED,
                ShippingEventType.SHIPPING_STARTED,
                trackingNumber,
                carrier,
                estimatedDeliveryDate,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * 배송 완료 이벤트 생성
     */
    public static ShippingEvent completed(
            String shipmentId,
            String orderId,
            String userId,
            String trackingNumber,
            LocalDateTime actualDeliveryDate
    ) {
        return new ShippingEvent(
                shipmentId,
                orderId,
                userId,
                ShippingStatus.SHIPPING_COMPLETED,
                ShippingEventType.SHIPPING_COMPLETED,
                trackingNumber,
                null,
                null,
                actualDeliveryDate,
                LocalDateTime.now()
        );
    }
}