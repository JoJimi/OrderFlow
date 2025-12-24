package org.example.inventory.repository.inventoryLog.adapter;

import lombok.RequiredArgsConstructor;
import org.example.inventory.domain.InventoryLog;
import org.example.inventory.repository.inventoryLog.InventoryLogRepository;
import org.example.inventory.repository.inventoryLog.SpringDataInventoryLogRepository;
import org.example.shared.type.inventory.InventoryAction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryLogRepositoryAdapter implements InventoryLogRepository {

    private final SpringDataInventoryLogRepository jpaRepository;

    @Override
    public InventoryLog save(InventoryLog log) {
        return jpaRepository.save(log);
    }

    @Override
    public List<InventoryLog> findByOrderIdAndAction(String orderId, InventoryAction action) {
        return jpaRepository.findByOrderIdAndAction(orderId, action);
    }
}