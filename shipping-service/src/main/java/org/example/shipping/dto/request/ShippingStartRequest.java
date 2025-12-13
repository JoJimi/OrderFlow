package org.example.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ShippingStartRequest(
        @NotBlank(message = "운송장 번호는 필수입니다")
        String trackingNumber,

        @NotBlank(message = "배송 업체는 필수입니다")
        String carrier,

        @NotNull(message = "예상 배송일은 필수입니다")
        LocalDate estimatedDeliveryDate
) {
}