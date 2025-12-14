package org.example.shipping.dto.response;

import org.example.shipping.domain.Shipment;
import org.example.shared.type.shipping.ShippingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShipmentResponse(
        String shipmentId,
        String orderId,
        ShippingStatus shippingStatus,
        String trackingNumber,
        String carrier,
        LocalDate estimatedDeliveryDate,
        LocalDateTime actualDeliveryDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getOrderId(),
                shipment.getShippingStatus(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getActualDeliveryDate(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}