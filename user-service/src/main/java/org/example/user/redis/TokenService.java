package org.example.user.redis;

import lombok.RequiredArgsConstructor;
import org.example.user.security.jwt.JwtProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository repository;
    private final JwtProperties jwtProperties;

    /**
     * 신규 세션 초기화 (로그인 시)
     * Redis HASH 타입으로 저장: refresh:{userId}
     */
    public void initSession(String userId, String familyId, String currentJti) {
        repository.saveSession(
                userId,
                familyId,
                currentJti,
                System.currentTimeMillis(),
                jwtProperties.getRefreshTokenExpirationMs()
        );
    }

    /**
     * 세션 조회
     */
    public RefreshSession getSession(String userId) {
        return repository.findSession(userId);
    }

    /**
     * 토큰 회전 (정상 갱신)
     * 1. 이전 jti를 블랙리스트에 추가 (남은 TTL 동안)
     * 2. 새 jti로 세션 갱신
     */
    public void rotate(String userId, String familyId, String oldJti, String newJti,
                       long oldTtlMs, long newTtlMs) {
        // 이전 jti 블랙리스트 (중복 사용 방지)
        repository.blacklistJti(oldJti, Math.max(oldTtlMs, 0));

        // 세션 업데이트
        repository.saveSession(
                userId,
                familyId,
                newJti,
                System.currentTimeMillis(),
                newTtlMs
        );
    }

    /**
     * 토큰 재사용 탐지 시 처리
     * 1. 재사용된 jti 블랙리스트
     * 2. 해당 familyId 전체 블랙리스트 (세션군 차단)
     * 3. 사용자 세션 삭제 (강제 로그아웃)
     */
    public void handleTokenReuse(String userId, String familyId, String jti, long ttlMs) {
        // 개별 토큰 블랙리스트
        repository.blacklistJti(jti, Math.max(ttlMs, 0));

        // 세션군 전체 블랙리스트 (보안 위협 대응)
        repository.blacklistFamily(familyId, Math.max(ttlMs, 0));

        // 세션 삭제 (재로그인 필요)
        repository.deleteSession(userId);
    }

    /**
     * Access Token 블랙리스트 추가 (로그아웃 시)
     */
    public void blacklistAccessToken(String jti, long ttlMs) {
        repository.blacklistJti(jti, Math.max(ttlMs, 0));
    }

    /**
     * JTI 블랙리스트 확인
     */
    public boolean isJtiBlacklisted(String jti) {
        return repository.isJtiBlacklisted(jti);
    }

    /**
     * Family 블랙리스트 확인
     */
    public boolean isFamilyBlacklisted(String fam) {
        return repository.isFamilyBlacklisted(fam);
    }

    /**
     * 세션 삭제 (로그아웃)
     */
    public void deleteSession(String userId) {
        repository.deleteSession(userId);
    }
}