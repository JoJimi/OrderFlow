package org.example.user.dto.response;

public record TokenResponse (
        String accessToken,
        String refreshToken
){ }
