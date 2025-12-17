package org.example.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
        @Valid
        @Schema(description = "주문 항목 목록", example = "[{\"productId\":\"SEED-1-000001\",\"quantity\":2}]")
        List<OrderItemRequest> items,

        @NotBlank(message = "배송 주소는 필수입니다")
        @Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123")
        String shippingAddress,

        @Schema(description = "배송 도시", example = "서울시")
        String shippingCity,

        @Schema(description = "우편번호", example = "06234")
        String shippingPostalCode
) {
    @Schema(description = "주문 항목")
    public record OrderItemRequest(
            @NotBlank(message = "상품 ID는 필수입니다")
            @Schema(description = "상품 ID", example = "SEED-1-000001")
            String productId,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 1 이상이어야 합니다")
            @Schema(description = "주문 수량", example = "2", minimum = "1")
            Integer quantity
    ) {}
}