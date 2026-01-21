package org.example.product.repository.elasticsearch;

import org.example.product.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch Repository
 *
 * Spring Data Elasticsearch가 자동으로 쿼리 메서드를 구현합니다.
 * @Query 어노테이션으로 커스텀 쿼리도 정의 가능합니다.
 */
@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * 상품명으로 검색 (전문 검색)
     *
     * @param productName 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    Page<ProductDocument> findByProductNameAndDeletedFalse(String productName, Pageable pageable);

    /**
     * 카테고리로 필터링
     */
    Page<ProductDocument> findByCategoryAndDeletedFalse(String category, Pageable pageable);

    /**
     * 가격 범위로 검색
     */
    Page<ProductDocument> findByPriceBetweenAndDeletedFalse(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * 카테고리 + 가격 범위 검색
     */
    Page<ProductDocument> findByCategoryAndPriceBetweenAndDeletedFalse(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * 커스텀 쿼리 - 다중 필드 검색 (Bool Query)
     *
     * productName 또는 description에서 keyword를 검색하면서
     * category로 필터링
     */
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["productName^2", "description"],
                  "fuzziness": "AUTO"
                }
              }
            ],
            "filter": [
              { "term": { "deleted": false } },
              { "term": { "category": "?1" } }
            ]
          }
        }
        """)
    Page<ProductDocument> searchByKeywordAndCategory(String keyword, String category, Pageable pageable);

    /**
     * 커스텀 쿼리 - 전문 검색 (Fuzzy 검색으로 오타 허용)
     */
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["productName^3", "productNameKeyword^2", "description"],
                  "fuzziness": "AUTO",
                  "prefix_length": 0,
                  "type": "best_fields"
                }
              }
            ],
            "filter": [
              { "term": { "deleted": false } }
            ]
          }
        }
        """)
    Page<ProductDocument> searchByMultiField(String keyword, Pageable pageable);

    /**
     * 삭제되지 않은 모든 상품 조회
     */
    Page<ProductDocument> findByDeletedFalse(Pageable pageable);

    /**
     * 특정 상품 ID로 조회
     */
    org.springframework.data.domain.Optional<ProductDocument> findByProductIdAndDeletedFalse(String productId);

    /**
     * 카테고리별 상품 수
     */
    long countByCategoryAndDeletedFalse(String category);

    /**
     * 모든 상품 삭제 (색인 초기화용)
     */
    void deleteAll();
}