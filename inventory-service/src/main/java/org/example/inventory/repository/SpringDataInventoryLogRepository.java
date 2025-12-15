package org.example.inventory.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.inventory.domain.InventoryLog;
import org.example.shared.type.inventory.InventoryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataInventoryLogRepository extends JpaRepository<InventoryLog, String> {

    @Query("SELECT il FROM InventoryLog il WHERE il.orderId = :orderId AND il.action = :action")
    List<InventoryLog> findByOrderIdAndAction(@Param("orderId") String orderId, @Param("action") InventoryAction action);
}