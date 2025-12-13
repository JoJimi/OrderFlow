package org.example.inventory.repository;

import org.example.inventory.domain.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    List<Inventory> saveAll(List<Inventory> inventories);
    Optional<Inventory> findById(String inventoryId);
    Optional<Inventory> findByProductId(String productId);
    List<Inventory> findByProductIdIn(List<String> productIds);
    boolean existsByProductId(String productId);
}