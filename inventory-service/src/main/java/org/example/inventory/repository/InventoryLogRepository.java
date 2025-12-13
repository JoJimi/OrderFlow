package org.example.inventory.repository;

import org.example.inventory.domain.InventoryLog;

public interface InventoryLogRepository {
    InventoryLog save(InventoryLog log);
}