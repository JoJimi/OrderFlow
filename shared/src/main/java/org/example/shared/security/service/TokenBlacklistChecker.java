package org.example.shared.security.service;

/**
 * 토큰 블랙리스트 확인 인터페이스
 * - user-service: Redis 기반 구현
 * - 다른 서비스: 구현 안 함 (null) 또는 다른 방식 구현
 */
public interface TokenBlacklistChecker {
    boolean isBlacklisted(String jti, String familyId);
}