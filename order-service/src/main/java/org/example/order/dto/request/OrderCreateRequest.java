package org.example.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
public record OrderCreateRequest(
        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "배송 주소는 필수입니다")
        String shippingAddress,

        String shippingCity,

        String shippingPostalCode
) {
    public record OrderItemRequest(
            @NotBlank(message = "상품 ID는 필수입니다")
            String productId,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 1 이상이어야 합니다")
            Integer quantity
    ) {}
}