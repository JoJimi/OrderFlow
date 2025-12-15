package org.example.order.dto.response;

import java.math.BigDecimal;

public record ProductInfoResponse(
        String productId,
        String productName,
        String description,
        BigDecimal price,
        String category
) {}
