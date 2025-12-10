package org.example.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.shared.type.CategoryType;

import java.math.BigDecimal;

@Schema(description = "상품 수정 요청 (부분 수정 지원)")
public record ProductUpdateRequest(
        @Size(min = 1, max = 255, message = "상품명은 1자 이상 255자 이하여야 합니다.")
        @Schema(description = "상품명", example = "갤럭시 S24 울트라 (업데이트)")
        String productName,

        @Size(min = 1, max = 2000, message = "상품 설명은 1자 이상 2000자 이하여야 합니다.")
        @Schema(description = "상품 설명", example = "최신 플래그십 스마트폰 - 업데이트된 설명")
        String description,

        @DecimalMin(value = "0.01", message = "가격은 0보다 커야 합니다.")
        @Digits(integer = 8, fraction = 2, message = "가격은 최대 8자리 정수와 2자리 소수로 구성되어야 합니다.")
        @Schema(description = "상품 가격", example = "1199000.00")
        BigDecimal price,

        @Schema(description = "상품 카테고리", example = "ELECTRONICS",
                allowableValues = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME", "SPORTS"})
        CategoryType category
) {
}