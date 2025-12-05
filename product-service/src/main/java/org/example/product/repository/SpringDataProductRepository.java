package org.example.product.repository;

import org.example.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA Repository
 * JPA 기반의 데이터 액세스를 담당합니다.
 */
public interface SpringDataProductRepository extends JpaRepository<Product, String> {

    /**
     * 삭제되지 않은 상품만 조회 (페이지네이션)
     */
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    /**
     * 카테고리별 상품 조회 (삭제되지 않은 것만)
     */
    Page<Product> findByIsDeletedFalseAndCategory(String category, Pageable pageable);

    /**
     * 상품명으로 검색 (삭제되지 않은 것만, LIKE 검색)
     */
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.productName LIKE %:keyword%")
    Page<Product> searchByProductName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 특정 ID의 상품 조회 (삭제되지 않은 것만)
     */
    Optional<Product> findByProductIdAndIsDeletedFalse(String productId);

    /**
     * 카테고리별 상품 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false AND p.category = :category")
    long countByCategory(@Param("category") String category);

    /**
     * 상품 ID로 삭제 여부 확인
     */
    @Query("SELECT p.isDeleted FROM Product p WHERE p.productId = :productId")
    Optional<Boolean> isProductDeleted(@Param("productId") String productId);

    /**
     * 특정 카테고리의 모든 상품을 논리 삭제 (대량 작업)
     */
    @Modifying
    @Query("UPDATE Product p SET p.isDeleted = true WHERE p.category = :category AND p.isDeleted = false")
    int deleteAllByCategory(@Param("category") String category);

    /**
     * 모든 상품 삭제 (테스트용)
     */
    @Modifying
    @Query("DELETE FROM Product p")
    void deleteAllProducts();
}