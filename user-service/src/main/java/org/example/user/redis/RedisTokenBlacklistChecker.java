package org.example.user.redis;

import lombok.RequiredArgsConstructor;
import org.example.shared.security.service.TokenBlacklistChecker;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 토큰 블랙리스트 확인 구현체
 */
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistChecker implements TokenBlacklistChecker {

    private final TokenService tokenService;

    @Override
    public boolean isBlacklisted(String jti, String familyId) {
        return tokenService.isJtiBlacklisted(jti)
                || tokenService.isFamilyBlacklisted(familyId);
    }
}