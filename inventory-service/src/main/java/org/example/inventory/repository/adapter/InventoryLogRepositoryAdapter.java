package org.example.inventory.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.inventory.domain.InventoryLog;
import org.example.inventory.repository.InventoryLogRepository;
import org.example.inventory.repository.SpringDataInventoryLogRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryLogRepositoryAdapter implements InventoryLogRepository {

    private final SpringDataInventoryLogRepository jpaRepository;

    @Override
    public InventoryLog save(InventoryLog log) {
        return jpaRepository.save(log);
    }
}