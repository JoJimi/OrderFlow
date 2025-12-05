package org.example.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 대량 초기화 응답")
public record ProductBulkInitResponse(
        @Schema(description = "총 생성된 상품 수", example = "100000")
        Integer totalCreated,

        @Schema(description = "배치 수", example = "100")
        Integer totalBatches,

        @Schema(description = "성공한 배치 수", example = "100")
        Integer successfulBatches,

        @Schema(description = "실패한 배치 수", example = "0")
        Integer failedBatches,

        @Schema(description = "처리 시간 (밀리초)", example = "45230")
        Long processingTimeMs,

        @Schema(description = "상태 메시지", example = "상품 대량 생성 완료")
        String message,

        @Schema(description = "실패한 배치 번호 목록 (있을 경우)")
        List<Integer> failedBatchNumbers
) {
}