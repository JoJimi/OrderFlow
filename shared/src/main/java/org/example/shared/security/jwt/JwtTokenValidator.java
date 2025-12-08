package org.example.shared.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 순수 JWT 검증 로직
 * - 블랙리스트 확인 없음 (각 서비스에서 필요 시 추가)
 * - 서명, 만료, 타입만 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "accessToken";
    private static final String REFRESH_TOKEN_TYPE = "refreshToken";

    private final JwtTokenProvider tokenProvider;

    /**
     * Access Token 검증
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS_TOKEN_TYPE);
    }

    /**
     * Refresh Token 검증
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, REFRESH_TOKEN_TYPE);
    }

    /**
     * 토큰 검증 공통 로직
     */
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = tokenProvider.parseClaims(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

            if (!expectedType.equals(tokenType)) {
                log.warn("토큰 타입 불일치. 예상: {}, 실제: {}", expectedType, tokenType);
                return false;
            }

            return !claims.getExpiration().before(new Date());

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT가 잘못되었습니다.", e);
        }
        return false;
    }

    /**
     * Claims 추출
     */
    public JwtClaims extractClaims(String token) {
        Claims claims = tokenProvider.parseClaims(token);

        return new JwtClaims(
                claims.getSubject(),
                claims.get("role", String.class),
                tokenProvider.getFamilyId(token),
                tokenProvider.getJti(token),
                claims.getExpiration()
        );
    }
}