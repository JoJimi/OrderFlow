package org.example.shipping.repository;

import org.example.shipping.domain.Shipment;

import java.util.Optional;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(String shipmentId);
    Optional<Shipment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}