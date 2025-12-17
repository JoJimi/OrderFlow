package org.example.shipping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "배송 시작 요청")
public record ShippingStartRequest(
        @NotBlank(message = "운송장 번호는 필수입니다")
        @Schema(description = "운송장 번호", example = "1234567890")
        String trackingNumber,

        @NotBlank(message = "배송 업체는 필수입니다")
        @Schema(description = "배송 업체", example = "CJ대한통운")
        String carrier,

        @NotNull(message = "예상 배송일은 필수입니다")
        @Schema(description = "예상 배송일", example = "2025-12-20")
        LocalDate estimatedDeliveryDate
) {
}