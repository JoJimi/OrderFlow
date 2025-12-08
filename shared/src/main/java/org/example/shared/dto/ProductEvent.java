package org.example.shared.dto;

import org.example.shared.type.ProductEventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 이벤트 DTO
 * Kafka를 통해 전파되는 상품 이벤트 데이터
 */
public record ProductEvent(
        ProductEventType eventType,
        String productId,
        String productName,
        String description,
        BigDecimal price,
        String category,
        LocalDateTime eventTimestamp,
        String eventId
) {
    /**
     * 상품 생성 이벤트 생성
     */
    public static ProductEvent created(
            String productId,
            String productName,
            String description,
            BigDecimal price,
            String category
    ) {
        return new ProductEvent(
                ProductEventType.PRODUCT_CREATED,
                productId,
                productName,
                description,
                price,
                category,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    /**
     * 상품 수정 이벤트 생성
     */
    public static ProductEvent updated(
            String productId,
            String productName,
            String description,
            BigDecimal price,
            String category
    ) {
        return new ProductEvent(
                ProductEventType.PRODUCT_UPDATED,
                productId,
                productName,
                description,
                price,
                category,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    /**
     * 상품 삭제 이벤트 생성
     */
    public static ProductEvent deleted(String productId) {
        return new ProductEvent(
                ProductEventType.PRODUCT_DELETED,
                productId,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    /**
     * 상품 대량 생성 이벤트 생성
     */
    public static ProductEvent bulkCreated(int count) {
        return new ProductEvent(
                ProductEventType.PRODUCT_BULK_CREATED,
                "BULK-" + count,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    /**
     * 이벤트 ID 생성 (UUID)
     */
    private static String generateEventId() {
        return java.util.UUID.randomUUID().toString();
    }
}
