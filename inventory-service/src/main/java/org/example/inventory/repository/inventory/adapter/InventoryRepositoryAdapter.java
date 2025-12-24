package org.example.inventory.repository.inventory.adapter;

import lombok.RequiredArgsConstructor;
import org.example.inventory.domain.Inventory;
import org.example.inventory.repository.inventory.InventoryRepository;
import org.example.inventory.repository.inventory.SpringDataInventoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final SpringDataInventoryRepository jpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        return jpaRepository.save(inventory);
    }

    @Override
    public List<Inventory> saveAll(List<Inventory> inventories) {  // ← 추가
        return jpaRepository.saveAll(inventories);
    }

    @Override
    public Optional<Inventory> findById(String inventoryId) {
        return jpaRepository.findById(inventoryId);
    }

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<Inventory> findByProductIdIn(List<String> productIds) {
        return jpaRepository.findByProductIdIn(productIds);
    }

    @Override
    public boolean existsByProductId(String productId) {
        return jpaRepository.existsByProductId(productId);
    }
}