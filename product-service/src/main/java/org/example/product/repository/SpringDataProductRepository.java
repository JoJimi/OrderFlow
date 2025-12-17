package org.example.product.repository;

import org.example.product.domain.Product;
import org.example.shared.type.product.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<Product, String> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByDeletedFalseAndCategory(CategoryType category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.productName LIKE %:keyword%")
    Page<Product> searchByProductName(@Param("keyword") String keyword, Pageable pageable);

    Optional<Product> findByProductIdAndDeletedFalse(String productId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.category = :category")
    long countByCategory(@Param("category") CategoryType category);

    @Query("SELECT p.deleted FROM Product p WHERE p.productId = :productId")
    Optional<Boolean> isProductDeleted(@Param("productId") String productId);

    @Modifying
    @Query("UPDATE Product p SET p.deleted = true WHERE p.category = :category AND p.deleted = false")
    int deleteAllByCategory(@Param("category") CategoryType category);

    @Modifying
    @Query("DELETE FROM Product p")
    void deleteAllProducts();
}