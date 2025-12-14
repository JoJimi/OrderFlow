package org.example.shared.dto;

import org.example.shared.type.shipping.ShippingEventType;
import org.example.shared.type.shipping.ShippingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

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