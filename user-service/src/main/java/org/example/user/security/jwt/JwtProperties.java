package org.example.user.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
// JWT 토큰 요소 정의
public class JwtProperties {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration.accessToken}")
    private Long accessTokenExpirationMs;

    @Value("${jwt.expiration.refreshToken}")
    private Long refreshTokenExpirationMs;

}
