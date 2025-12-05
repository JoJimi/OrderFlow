package org.example.product.repository;

import org.example.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository Port
 * 비즈니스 로직에서 필요한 데이터 액세스 메서드를 정의합니다.
 */
public interface ProductRepository {

    /**
     * 상품 ID로 조회 (삭제되지 않은 것만)
     */
    Optional<Product> findById(String productId);

    /**
     * 모든 상품 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * 카테고리별 상품 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * 상품명으로 검색 (LIKE 검색, 페이지네이션)
     */
    Page<Product> searchByProductName(String keyword, Pageable pageable);

    /**
     * 상품 저장
     */
    Product save(Product product);

    /**
     * 상품 목록 일괄 저장 (대량 삽입)
     */
    List<Product> saveAll(List<Product> products);

    /**
     * 상품 삭제 여부 확인
     */
    boolean isDeleted(String productId);

    /**
     * 카테고리별 상품 개수
     */
    long countByCategory(String category);

    /**
     * 모든 상품 삭제 (테스트용)
     */
    void deleteAll();
}