package org.example.inventory.repository;

import org.example.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataInventoryRepository extends JpaRepository<Inventory, String> {

    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.deleted = false")
    Optional<Inventory> findByProductId(@Param("productId") String productId);

    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds AND i.deleted = false")
    List<Inventory> findByProductIdIn(@Param("productIds") List<String> productIds);

    @Query("SELECT COUNT(i) > 0 FROM Inventory i WHERE i.productId = :productId AND i.deleted = false")
    boolean existsByProductId(@Param("productId") String productId);
}