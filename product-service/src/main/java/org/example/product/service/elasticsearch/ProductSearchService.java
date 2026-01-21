package org.example.product.service.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.document.ProductDocument;
import org.example.product.dto.response.ProductResponse;
import org.example.product.repository.elasticsearch.ProductElasticsearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch 기반 상품 검색 서비스
 *
 * 기능:
 * - 전문 검색 (Full-Text Search)
 * - 필터링 (카테고리, 가격대)
 * - 페이지네이션 및 정렬
 * - 오타 허용 검색 (Fuzzy Search)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductElasticsearchRepository elasticsearchRepository;

    /**
     * 키워드 기반 상품 검색
     *
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    public Page<ProductResponse> searchByKeyword(String keyword, Pageable pageable) {
        long startTime = System.currentTimeMillis();

        try {
            Page<ProductDocument> results = elasticsearchRepository
                    .searchByMultiField(keyword, pageable);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Elasticsearch] 키워드 검색 완료 - keyword: {}, 결과: {} 건, 소요시간: {} ms",
                    keyword, results.getTotalElements(), duration);

            return results.map(ProductDocument::toResponse);

        } catch (Exception e) {
            log.error("[Elasticsearch] 검색 중 오류 발생 - keyword: {}", keyword, e);
            throw new RuntimeException("검색 서비스 오류", e);
        }
    }

    /**
     * 카테고리별 검색
     *
     * @param category 카테고리 코드
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    public Page<ProductResponse> searchByCategory(String category, Pageable pageable) {
        long startTime = System.currentTimeMillis();

        try {
            Page<ProductDocument> results = elasticsearchRepository
                    .findByCategoryAndDeletedFalse(category, pageable);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Elasticsearch] 카테고리 검색 완료 - category: {}, 결과: {} 건, 소요시간: {} ms",
                    category, results.getTotalElements(), duration);

            return results.map(ProductDocument::toResponse);

        } catch (Exception e) {
            log.error("[Elasticsearch] 카테고리 검색 중 오류 발생 - category: {}", category, e);
            throw new RuntimeException("검색 서비스 오류", e);
        }
    }

    /**
     * 가격 범위로 검색
     *
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    public Page<ProductResponse> searchByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        long startTime = System.currentTimeMillis();

        try {
            Page<ProductDocument> results = elasticsearchRepository
                    .findByPriceBetweenAndDeletedFalse(minPrice, maxPrice, pageable);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Elasticsearch] 가격 범위 검색 완료 - minPrice: {}, maxPrice: {}, 결과: {} 건, 소요시간: {} ms",
                    minPrice, maxPrice, results.getTotalElements(), duration);

            return results.map(ProductDocument::toResponse);

        } catch (Exception e) {
            log.error("[Elasticsearch] 가격 검색 중 오류 발생 - minPrice: {}, maxPrice: {}",
                    minPrice, maxPrice, e);
            throw new RuntimeException("검색 서비스 오류", e);
        }
    }

    /**
     * 키워드 + 카테고리 복합 검색
     *
     * @param keyword 검색 키워드
     * @param category 카테고리
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    public Page<ProductResponse> searchByKeywordAndCategory(
            String keyword,
            String category,
            Pageable pageable
    ) {
        long startTime = System.currentTimeMillis();

        try {
            Page<ProductDocument> results = elasticsearchRepository
                    .searchByKeywordAndCategory(keyword, category, pageable);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Elasticsearch] 복합 검색 완료 - keyword: {}, category: {}, 결과: {} 건, 소요시간: {} ms",
                    keyword, category, results.getTotalElements(), duration);

            return results.map(ProductDocument::toResponse);

        } catch (Exception e) {
            log.error("[Elasticsearch] 복합 검색 중 오류 발생 - keyword: {}, category: {}",
                    keyword, category, e);
            throw new RuntimeException("검색 서비스 오류", e);
        }
    }

    /**
     * Elasticsearch 색인에 상품 문서 저장
     * (DB 저장 후 비동기로 호출되어야 함)
     *
     * @param document 저장할 상품 문서
     */
    public void indexProduct(ProductDocument document) {
        try {
            elasticsearchRepository.save(document);
            log.debug("[Elasticsearch] 상품 색인 저장 완료 - productId: {}", document.getProductId());
        } catch (Exception e) {
            log.error("[Elasticsearch] 상품 색인 저장 실패 - productId: {}", document.getProductId(), e);
        }
    }

    /**
     * 특정 상품 색인 삭제
     *
     * @param productId 상품 ID
     */
    public void deleteIndex(String productId) {
        try {
            elasticsearchRepository.deleteById(productId);
            log.debug("[Elasticsearch] 상품 색인 삭제 완료 - productId: {}", productId);
        } catch (Exception e) {
            log.error("[Elasticsearch] 상품 색인 삭제 실패 - productId: {}", productId, e);
        }
    }

    /**
     * 모든 색인 초기화 (대량 재색인)
     */
    public void clearAllIndexes() {
        try {
            elasticsearchRepository.deleteAll();
            log.info("[Elasticsearch] 모든 색인 삭제 완료");
        } catch (Exception e) {
            log.error("[Elasticsearch] 색인 초기화 실패", e);
        }
    }

    /**
     * 색인 건강 상태 확인
     *
     * @return true if 색인이 정상, false otherwise
     */
    public boolean isHealthy() {
        try {
            // 간단한 건강 체크: 빈 검색 실행
            elasticsearchRepository.findByDeletedFalse(Pageable.ofSize(1));
            return true;
        } catch (Exception e) {
            log.error("[Elasticsearch] 색인 건강 체크 실패", e);
            return false;
        }
    }
}