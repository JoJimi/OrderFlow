package org.example.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "상품 대량 초기화 요청 (테스트용)")
public record ProductBulkInitRequest(
        @Min(value = 1, message = "생성할 상품 개수는 최소 1개 이상이어야 합니다.")
        @Max(value = 1000000, message = "생성할 상품 개수는 최대 100만개까지 가능합니다.")
        @Schema(description = "생성할 상품 개수", example = "100000", defaultValue = "100000")
        Integer count,

        @Min(value = 100, message = "배치 크기는 최소 100 이상이어야 합니다.")
        @Max(value = 10000, message = "배치 크기는 최대 10000까지 가능합니다.")
        @Schema(description = "배치 삽입 단위", example = "1000", defaultValue = "1000")
        Integer batchSize
) {
    public ProductBulkInitRequest {
        if (count == null) {
            count = 100000;
        }
        if (batchSize == null) {
            batchSize = 1000;
        }
    }

    public ProductBulkInitRequest() {
        this(100000, 1000);
    }
}