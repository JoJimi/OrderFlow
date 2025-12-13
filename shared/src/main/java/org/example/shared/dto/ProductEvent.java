package org.example.shared.dto;

import org.example.shared.type.ProductEventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductEvent(
        ProductEventType eventType,
        String productId,
        String productName,
        String description,
        BigDecimal price,
        String category,
        Integer count,
        LocalDateTime eventTimestamp,
        String eventId
) {

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
                null,
                LocalDateTime.now(),
                generateEventId()
        );
    }

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
                null,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    public static ProductEvent deleted(String productId) {
        return new ProductEvent(
                ProductEventType.PRODUCT_DELETED,
                productId,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    public static ProductEvent bulkCreated(int count) {
        return new ProductEvent(
                ProductEventType.PRODUCT_BULK_CREATED,
                null,
                null,
                null,
                null,
                null,
                count,
                LocalDateTime.now(),
                generateEventId()
        );
    }

    private static String generateEventId() {
        return java.util.UUID.randomUUID().toString();
    }
}