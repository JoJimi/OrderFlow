package org.example.product.repository;

import org.example.product.domain.Product;
import org.example.shared.type.product.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(String productId);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategory(CategoryType category, Pageable pageable);
    Page<Product> searchByProductName(String keyword, Pageable pageable);
    Product save(Product product);
    List<Product> saveAll(List<Product> products);
    boolean isDeleted(String productId);
    long countByCategory(CategoryType category);
    void deleteAll();
}