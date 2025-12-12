package org.example.inventory.repository;

import org.example.inventory.domain.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * 재고 저장소 인터페이스 (도메인 계층 Port)
 */
public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findById(String inventoryId);
    Optional<Inventory> findByProductId(String productId);
    List<Inventory> findByProductIdIn(List<String> productIds);
    boolean existsByProductId(String productId);
}