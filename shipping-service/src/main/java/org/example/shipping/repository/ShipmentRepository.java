package org.example.shipping.repository;

import org.example.shipping.domain.Shipment;

import java.util.Optional;

/**
 * 배송 저장소 인터페이스 (도메인 계층 Port)
 */
public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(String shipmentId);
    Optional<Shipment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}