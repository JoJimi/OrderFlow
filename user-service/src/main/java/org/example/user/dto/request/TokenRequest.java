package org.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "Refresh Token 이 누락되었습니다.")
        String refreshToken
) { }
