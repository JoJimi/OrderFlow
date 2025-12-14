package org.example.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.domain.Product;
import org.example.product.dto.request.ProductBulkInitRequest;
import org.example.product.dto.request.ProductCreateRequest;
import org.example.product.dto.request.ProductUpdateRequest;
import org.example.product.dto.response.ProductBulkInitResponse;
import org.example.product.dto.response.ProductResponse;
import org.example.product.repository.ProductBatchInsertRepository;
import org.example.product.repository.ProductRepository;
import org.example.shared.dto.ProductEvent;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;
import org.example.shared.type.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheService cacheService;
    private final ProductBatchInsertRepository batchInsertRepository;
    private final ProductEventProducer eventProducer;

    private static final int MIN_PRICE = 1000;
    private static final int MAX_PRICE = 999999;

    /**
     * 전체 상품 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("전체 상품 목록 조회 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable)
                .map(ProductResponse::from);
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categoryCode, Pageable pageable) {
        log.info("카테고리별 상품 조회 - 카테고리: {}, 페이지: {}", categoryCode, pageable.getPageNumber());

        CategoryType category = CategoryType.from(categoryCode);

        return productRepository.findByCategory(category, pageable)
                .map(ProductResponse::from);
    }

    /**
     * 상품 상세 조회 (캐시 우선)
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String productId) {
        log.info("상품 상세 조회 - 상품 ID: {}", productId);

        // 1. 캐시에서 조회
        ProductResponse cachedProduct = cacheService.getProductFromCache(productId);
        if (cachedProduct != null) {
            log.debug("캐시에서 상품 조회 성공 - 상품 ID: {}", productId);
            return cachedProduct;
        }

        // 2. DB에서 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductResponse response = ProductResponse.from(product);

        // 3. 캐시에 저장
        cacheService.cacheProduct(productId, response);

        return response;
    }

    /**
     * 상품 검색 (상품명 기반)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        log.info("상품 검색 - 키워드: {}, 페이지: {}", keyword, pageable.getPageNumber());
        return productRepository.searchByProductName(keyword, pageable)
                .map(ProductResponse::from);
    }

    /**
     * 상품 등록 (관리자)
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("상품 등록 - 상품명: {}, 카테고리: {}", request.productName(), request.category());

        String productId = "PROD-" + UUID.randomUUID();

        Product product = Product.builder()
                .productId(productId)
                .productName(request.productName())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .build();

        Product savedProduct = productRepository.save(product);
        ProductResponse response = ProductResponse.from(savedProduct);

        // 캐시에 저장
        cacheService.cacheProduct(productId, response);

        // Kafka 이벤트 발행
        publishProductCreatedEvent(savedProduct);

        log.info("상품 등록 완료 - 상품 ID: {}", productId);

        return response;
    }

    /**
     * 상품 수정 (관리자)
     */
    @Transactional
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request) {
        log.info("상품 수정 - 상품 ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 부분 수정 지원
        product.updateInfo(
                request.productName(),
                request.description(),
                request.price(),
                request.category()
        );

        Product updatedProduct = productRepository.save(product);
        ProductResponse response = ProductResponse.from(updatedProduct);

        // 캐시 갱신
        cacheService.evictProduct(productId);
        cacheService.cacheProduct(productId, response);

        // Kafka 이벤트 발행
        publishProductUpdatedEvent(updatedProduct);

        log.info("상품 수정 완료 - 상품 ID: {}", productId);

        return response;
    }

    /**
     * 상품 삭제 (논리 삭제)
     */
    @Transactional
    public void deleteProduct(String productId) {
        log.info("상품 삭제 - 상품 ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.markAsDeleted(); // BaseEntity의 setDeleted(true) 호출

        productRepository.save(product);

        // 캐시에서 삭제
        cacheService.evictProduct(productId);

        // Kafka 이벤트 발행
        ProductEvent event = ProductEvent.deleted(productId);
        eventProducer.publishProductDeletedEvent(event);

        log.info("상품 삭제 완료 - 상품 ID: {}", productId);
    }

    /**
     * 상품 대량 추가 - JDBC Batch 사용 (고성능)
     */
    public ProductBulkInitResponse bulkInitProducts(ProductBulkInitRequest request) {
        log.info("상품 대량 추가 시작 (JDBC Batch) - 개수: {}, 배치 크기: {}",
                request.count(), request.batchSize());

        long startTime = System.currentTimeMillis();

        int totalCount = request.count();
        int batchSize = request.batchSize();
        int totalBatches = (int) Math.ceil((double) totalCount / batchSize);
        int successfulBatches = 0;
        int failedBatches = 0;
        List<Integer> failedBatchNumbers = new ArrayList<>();

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int startIdx = batchNum * batchSize;
            int endIdx = Math.min(startIdx + batchSize, totalCount);

            try {
                saveBatchWithJdbc(startIdx, endIdx);
                successfulBatches++;

                if ((batchNum + 1) % 10 == 0) {
                    log.info("진행 상황: {} / {} 배치 완료 ({} %)",
                            batchNum + 1, totalBatches,
                            (int)((batchNum + 1) * 100.0 / totalBatches));
                }

            } catch (Exception e) {
                log.error("배치 {} 저장 실패", batchNum + 1, e);
                failedBatches++;
                failedBatchNumbers.add(batchNum + 1);
            }
        }

        // 캐시 초기화
        cacheService.evictAllProducts();

        long processingTime = System.currentTimeMillis() - startTime;
        int totalCreated = totalCount - (failedBatches * batchSize);

        log.info("상품 대량 추가 완료 - 총 생성: {} 개, 소요 시간: {} ms ({} 초)",
                totalCreated, processingTime, processingTime / 1000.0);

        return new ProductBulkInitResponse(
                totalCreated,
                totalBatches,
                successfulBatches,
                failedBatches,
                processingTime,
                failedBatches == 0 ? "상품 대량 생성 완료" : "일부 배치 생성 실패",
                failedBatches > 0 ? failedBatchNumbers : null
        );
    }

    /**
     * JDBC Batch로 배치 저장 (독립 트랜잭션)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatchWithJdbc(int startIdx, int endIdx) {
        List<Product> products = new ArrayList<>(endIdx - startIdx);

        for (int i = startIdx; i < endIdx; i++) {
            Product product = Product.builder()
                    .productId("SEED-1-" + String.format("%06d", i + 1))
                    .productName("테스트 상품 " + (i + 1))
                    .description("대량 생성된 테스트 상품입니다. (번호: " + (i + 1) + ")")
                    .price(generateRandomPrice())
                    .category(CategoryType.getRandomCategory())
                    .build();
            products.add(product);
        }

        batchInsertRepository.batchInsert(products);

        // 각 상품마다 PRODUCT_CREATED 이벤트 발행 (재고 초기화용)
        products.forEach(this::publishProductCreatedEvent);
    }

    /**
     * 랜덤 가격 생성
     */
    private BigDecimal generateRandomPrice() {
        int price = ThreadLocalRandom.current().nextInt(MIN_PRICE, MAX_PRICE + 1);
        return BigDecimal.valueOf(price);
    }

    /**
     * 상품 생성 이벤트 발행
     */
    private void publishProductCreatedEvent(Product product) {
        ProductEvent event = ProductEvent.created(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getCode()
        );
        eventProducer.publishProductCreatedEvent(event);
    }

    /**
     * 상품 수정 이벤트 발행
     */
    private void publishProductUpdatedEvent(Product product) {
        ProductEvent event = ProductEvent.updated(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getCode()
        );
        eventProducer.publishProductUpdatedEvent(event);
    }
}