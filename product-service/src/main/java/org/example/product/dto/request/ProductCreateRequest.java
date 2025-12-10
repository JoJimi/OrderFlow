package org.example.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.shared.type.CategoryType;

import java.math.BigDecimal;

@Schema(description = "상품 생성 요청")
public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(min = 1, max = 255, message = "상품명은 1자 이상 255자 이하여야 합니다.")
        @Schema(description = "상품명", example = "갤럭시 S24 울트라", required = true)
        String productName,

        @NotBlank(message = "상품 설명은 필수입니다.")
        @Size(min = 1, max = 2000, message = "상품 설명은 1자 이상 2000자 이하여야 합니다.")
        @Schema(description = "상품 설명", example = "최신 플래그십 스마트폰", required = true)
        String description,

        @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0.01", message = "가격은 0보다 커야 합니다.")
        @Digits(integer = 8, fraction = 2, message = "가격은 최대 8자리 정수와 2자리 소수로 구성되어야 합니다.")
        @Schema(description = "상품 가격", example = "1299000.00", required = true)
        BigDecimal price,

        @NotNull(message = "초기 재고는 필수입니다.")
        @Min(value = 0, message = "초기 재고는 0 이상이어야 합니다.")
        @Schema(description = "초기 재고 수량", example = "100", required = true)
        Integer initialStock,

        @NotNull(message = "카테고리는 필수입니다.")
        @Schema(description = "상품 카테고리", example = "ELECTRONICS",
                allowableValues = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME", "SPORTS"},
                required = true)
        CategoryType category
) {
}