package org.example.inventory.repository;

import org.example.inventory.domain.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInventoryLogRepository extends JpaRepository<InventoryLog, String> {
}