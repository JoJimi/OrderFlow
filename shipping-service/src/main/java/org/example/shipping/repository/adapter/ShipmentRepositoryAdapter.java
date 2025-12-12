package org.example.shipping.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.shipping.domain.Shipment;
import org.example.shipping.repository.ShipmentRepository;
import org.example.shipping.repository.SpringDataShipmentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryAdapter implements ShipmentRepository {

    private final SpringDataShipmentRepository jpaRepository;

    @Override
    public Shipment save(Shipment shipment) {
        return jpaRepository.save(shipment);
    }

    @Override
    public Optional<Shipment> findById(String shipmentId) {
        return jpaRepository.findById(shipmentId);
    }

    @Override
    public Optional<Shipment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}