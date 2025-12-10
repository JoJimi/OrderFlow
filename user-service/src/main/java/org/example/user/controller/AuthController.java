package org.example.user.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.exception.auth.InvalidTokenException;
import org.example.shared.security.jwt.JwtTokenProvider;
import org.example.user.dto.request.TokenRequest;
import org.example.user.dto.response.TokenResponse;
import org.example.user.redis.RefreshSession;
import org.example.user.redis.TokenService;
import org.example.user.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtProvider;
    private final TokenService tokenService;

    /**
     * Refresh Token으로 새로운 Access Token과 Refresh Token 발급
     * 토큰 로테이션 전략 적용: 매번 새로운 토큰 쌍 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRequest request) {
        String refreshToken = request.refreshToken();

        // 1. 기본 검증: 서명, 만료, 타입
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        // 2. 토큰에서 클레임 추출
        Claims claims = extractClaims(refreshToken);
        String userId = claims.getSubject();
        String role = jwtProvider.getRole(refreshToken);
        String fam = jwtProvider.getFamilyId(refreshToken);
        String jti = jwtProvider.getJti(refreshToken);
        Date exp = claims.getExpiration();

        // 3. 남은 TTL 계산
        long remainTtlMs = calculateRemainTtl(exp);

        // 4. 블랙리스트 검증
        validateNotBlacklisted(jti, fam);

        // 5. Redis 세션 검증
        RefreshSession session = validateSession(userId, fam);

        // 6. 재사용 탐지 또는 정상 회전
        if (isTokenReused(jti, session.currentJti())) {
            handleTokenReuse(userId, fam, jti, remainTtlMs);
            throw new InvalidTokenException();
        }

        // 7. 정상 회전: 새 토큰 발급 및 세션 업데이트
        return ResponseEntity.ok(rotateTokens(userId, fam, role, jti, remainTtlMs));
    }

    /**
     * 로그아웃: Redis 세션 삭제 + Access Token 블랙리스트 추가
     * - Refresh Token: 세션 삭제로 무효화
     * - Access Token: 블랙리스트에 추가하여 즉시 무효화
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String userId = principal.getUser().getId().toString();

        // Access Token 블랙리스트 추가
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String jti = jwtProvider.getJti(accessToken);
                long remainTtl = calculateRemainTtl(jwtProvider.getExpiration(accessToken));
                tokenService.blacklistAccessToken(jti, remainTtl);
            } catch (Exception e) {
                // 토큰 파싱 실패 시 무시 (이미 유효하지 않은 토큰)
                log.warn("로그아웃 시 Access Token 파싱 실패: {}", e.getMessage());
            }
        }

        // Refresh Token 세션 삭제
        tokenService.deleteSession(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh Token에서 Claims 추출
     */
    private Claims extractClaims(String refreshToken) {
        try {
            return jwtProvider.parseClaims(refreshToken);
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    /**
     * 토큰의 남은 유효 시간 계산
     */
    private long calculateRemainTtl(Date expiration) {
        long nowMs = System.currentTimeMillis();
        return expiration.getTime() - nowMs;
    }

    /**
     * 블랙리스트 검증
     */
    private void validateNotBlacklisted(String jti, String fam) {
        if (tokenService.isJtiBlacklisted(jti) || tokenService.isFamilyBlacklisted(fam)) {
            throw new InvalidTokenException();
        }
    }

    /**
     * Redis 세션 검증
     */
    private RefreshSession validateSession(String userId, String fam) {
        RefreshSession session = tokenService.getSession(userId);

        if (session == null) {
            throw new InvalidTokenException();
        }

        // 세션군(familyId) 불일치 검증
        if (!fam.equals(session.familyId())) {
            throw new InvalidTokenException();
        }

        return session;
    }

    /**
     * 토큰 재사용 여부 확인
     */
    private boolean isTokenReused(String jti, String currentJti) {
        return !jti.equals(currentJti);
    }

    /**
     * 토큰 재사용 탐지 시 처리
     */
    private void handleTokenReuse(String userId, String fam, String jti, long ttlMs) {
        tokenService.handleTokenReuse(userId, fam, jti, ttlMs);
    }

    /**
     * 정상 토큰 회전: 새 토큰 발급 및 세션 업데이트
     */
    private TokenResponse rotateTokens(String userId, String fam, String role, String oldJti, long oldTtlMs) {  // ← role 파라미터 추가
        log.debug("토큰 회전 - userId: {}, role: {}, oldJti: {}", userId, role, oldJti);

        // 새 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(userId, fam, role);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId, fam, role);
        String newJti = jwtProvider.getJti(newRefreshToken);

        // 새 토큰의 TTL 계산
        long newTtlMs = calculateRemainTtl(jwtProvider.getExpiration(newRefreshToken));

        // 세션 회전: 이전 jti 블랙리스트 + 새 jti로 세션 갱신
        tokenService.rotate(userId, fam, oldJti, newJti, oldTtlMs, newTtlMs);

        log.info("토큰 회전 완료 - userId: {}, role: {}, newJti: {}", userId, role, newJti);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}