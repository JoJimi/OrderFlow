package org.example.shipping.repository;

import org.example.shipping.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataShipmentRepository extends JpaRepository<Shipment, String> {

    @Query("SELECT s FROM Shipment s WHERE s.orderId = :orderId AND s.deleted = false")
    Optional<Shipment> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT COUNT(s) > 0 FROM Shipment s WHERE s.orderId = :orderId AND s.deleted = false")
    boolean existsByOrderId(@Param("orderId") String orderId);
}