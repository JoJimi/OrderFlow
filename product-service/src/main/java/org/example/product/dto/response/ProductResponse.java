package org.example.product.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.product.domain.Product;
import org.example.shared.type.product.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "상품 응답")
public record ProductResponse(
        @Schema(description = "상품 ID", example = "PROD-550e8400-e29b-41d4-a716-446655440000")
        String productId,

        @Schema(description = "상품명", example = "갤럭시 S24 울트라")
        String productName,

        @Schema(description = "상품 설명", example = "최신 플래그십 스마트폰")
        String description,

        @Schema(description = "상품 가격", example = "1299000.00")
        BigDecimal price,

        @Schema(description = "상품 카테고리", example = "ELECTRONICS")
        CategoryType category,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "생성 시간", example = "2024-12-10 10:30:00")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "수정 시간", example = "2024-12-10 15:45:00")
        LocalDateTime updatedAt,

        @Schema(description = "삭제 여부", example = "false")
        boolean isDelete
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.isDeleted()
        );
    }
}