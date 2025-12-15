package org.example.inventory.repository;

import org.example.inventory.domain.InventoryLog;
import org.example.shared.type.inventory.InventoryAction;

import java.util.List;

public interface InventoryLogRepository {
    InventoryLog save(InventoryLog log);
    List<InventoryLog> findByOrderIdAndAction(String orderId, InventoryAction action);
}