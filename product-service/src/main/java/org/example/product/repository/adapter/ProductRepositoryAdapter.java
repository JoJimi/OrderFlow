package org.example.product.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.product.domain.Product;
import org.example.product.repository.ProductRepository;
import org.example.product.repository.SpringDataProductRepository;
import org.example.shared.type.product.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository springDataProductRepository;

    @Override
    public Optional<Product> findById(String productId) {
        return springDataProductRepository.findByProductIdAndDeletedFalse(productId);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return springDataProductRepository.findByDeletedFalse(pageable);
    }

    @Override
    public Page<Product> findByCategory(CategoryType category, Pageable pageable) {
        return springDataProductRepository.findByDeletedFalseAndCategory(category, pageable);
    }

    @Override
    public Page<Product> searchByProductName(String keyword, Pageable pageable) {
        return springDataProductRepository.searchByProductName(keyword, pageable);
    }

    @Override
    public Product save(Product product) {
        return springDataProductRepository.save(product);
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        return springDataProductRepository.saveAll(products);
    }

    @Override
    public boolean isDeleted(String productId) {
        return springDataProductRepository.isProductDeleted(productId)
                .orElse(false);
    }

    @Override
    public long countByCategory(CategoryType category) {
        return springDataProductRepository.countByCategory(category);
    }

    @Override
    public void deleteAll() {
        springDataProductRepository.deleteAllProducts();
    }
}