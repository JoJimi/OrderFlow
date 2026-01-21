package org.example.product.service.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.document.ProductDocument;
import org.example.product.domain.Product;
import org.example.product.repository.ProductRepository;
import org.example.product.repository.elasticsearch.ProductElasticsearchRepository;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 색인 초기화 서비스
 *
 * 애플리케이션 시작 시 DB의 모든 상품을 Elasticsearch에 색인합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductIndexInitializer {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;

    private static final int BATCH_SIZE = 1000;

    /**
     * 애플리케이션 시작 시 자동으로 색인 초기화
     */
    @EventListener(ApplicationStartedEvent.class)
    public void initializeIndexOnStartup() {
        log.info("[Elasticsearch] 색인 초기화 시작...");
        try {
            reindexAllProducts();
            log.info("[Elasticsearch] 색인 초기화 완료");
        } catch (Exception e) {
            log.error("[Elasticsearch] 색인 초기화 실패", e);
        }
    }

    /**
     * 모든 상품을 Elasticsearch에 색인
     */
    public void reindexAllProducts() {
        long startTime = System.currentTimeMillis();

        try {
            log.info("[Elasticsearch] 기존 색인 삭제 중...");
            elasticsearchRepository.deleteAll();

            int pageNumber = 0;
            int totalIndexed = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Product> productPage = productRepository.findAll(pageable);

                if (productPage.isEmpty()) {
                    break;
                }

                var documents = productPage.getContent()
                        .stream()
                        .map(ProductDocument::from)
                        .toList();

                elasticsearchRepository.saveAll(documents);

                totalIndexed += documents.size();

                if ((pageNumber + 1) % 10 == 0) {
                    log.info("[Elasticsearch] 색인 중... {}/{}",
                            totalIndexed, productPage.getTotalElements());
                }

                pageNumber++;

                if (!productPage.hasNext()) {
                    break;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Elasticsearch] 색인 완료 - 총 {} 건, 소요시간: {} ms", totalIndexed, duration);

        } catch (Exception e) {
            log.error("[Elasticsearch] 색인 실패", e);
            throw new RuntimeException("Elasticsearch 색인 초기화 실패", e);
        }
    }

    /**
     * 특정 상품만 색인
     */
    public void indexProduct(String productId) {
        try {
            productRepository.findById(productId).ifPresent(product -> {
                ProductDocument document = ProductDocument.from(product);
                elasticsearchRepository.save(document);
                log.debug("[Elasticsearch] 상품 색인 저장 - productId: {}", productId);
            });
        } catch (Exception e) {
            log.error("[Elasticsearch] 상품 색인 저장 실패 - productId: {}", productId, e);
        }
    }

    /**
     * 특정 상품 색인 삭제
     */
    public void deleteIndex(String productId) {
        try {
            elasticsearchRepository.deleteById(productId);
            log.debug("[Elasticsearch] 상품 색인 삭제 - productId: {}", productId);
        } catch (Exception e) {
            log.error("[Elasticsearch] 상품 색인 삭제 실패 - productId: {}", productId, e);
        }
    }
}