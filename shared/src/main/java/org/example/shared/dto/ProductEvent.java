package org.example.shared.dto;

import org.example.shared.type.product.ProductEventType;
import org.example.shared.util.IdGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductEvent(
        String eventId,
        ProductEventType eventType,
        String productId,
        String productName,
        String description,
        BigDecimal price,
        String category,
        String metadata,
        LocalDateTime eventTimestamp
) {

    public static ProductEvent created(
            String productId,
            String productName,
            String description,
            BigDecimal price,
            String category
    ) {
        return new ProductEvent(
                IdGenerator.generateEventId(),
                ProductEventType.PRODUCT_CREATED,
                productId,
                productName,
                description,
                price,
                category,
                null,
                LocalDateTime.now()
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
                IdGenerator.generateEventId(),
                ProductEventType.PRODUCT_UPDATED,
                productId,
                productName,
                description,
                price,
                category,
                null,
                LocalDateTime.now()
        );
    }

    public static ProductEvent deleted(String productId) {
        return new ProductEvent(
                IdGenerator.generateEventId(),
                ProductEventType.PRODUCT_DELETED,
                productId,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static ProductEvent bulkCreated(int count) {
        return new ProductEvent(
                IdGenerator.generateEventId(),
                ProductEventType.PRODUCT_BULK_CREATED,
                null,
                null,
                null,
                null,
                null,
                String.valueOf(count),
                LocalDateTime.now()
        );
    }
}