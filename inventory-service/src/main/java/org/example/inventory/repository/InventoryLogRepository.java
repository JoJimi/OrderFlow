package org.example.inventory.repository;

import org.example.inventory.domain.InventoryLog;

/**
 * 재고 로그 저장소 인터페이스
 */
public interface InventoryLogRepository {
    InventoryLog save(InventoryLog log);
}