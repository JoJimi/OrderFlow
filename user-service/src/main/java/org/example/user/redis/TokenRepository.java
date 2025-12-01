package org.example.user.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final StringRedisTemplate redisTemplate;

    // === 키 프리픽스 상수 ===
    private static final String SESSION_PREFIX = "refresh:";
    private static final String BL_RT_PREFIX = "blacklist:rt:";
    private static final String BL_FAM_PREFIX = "blacklist:fam:";

    // === 세션 필드명 상수 ===
    private static final String FIELD_FAMILY_ID = "familyId";
    private static final String FIELD_CURRENT_JTI = "currentJti";
    private static final String FIELD_ROTATED_AT = "rotatedAt";

    // === 키 생성 헬퍼 메서드 ===

    private String keySession(String userId) {
        return SESSION_PREFIX + userId;
    }

    private String keyRtBlacklist(String jti) {
        return BL_RT_PREFIX + jti;
    }

    private String keyFamBlacklist(String fam) {
        return BL_FAM_PREFIX + fam;
    }

    // === 타입 가드 ===

    /**
     * 키가 HASH 타입이 아니면 삭제
     * 목적: 과거 버전과의 키 충돌 방지 (String → HASH 마이그레이션)
     *
     * 예: 과거에 refresh:{userId}를 String으로 저장했다면,
     *     현재 HASH로 저장 시 WRONGTYPE 에러 발생
     *     → 기존 키 삭제 후 새로 저장
     */
    private void ensureHashKey(String key) {
        DataType type = redisTemplate.type(key);
        if (type != null && type != DataType.NONE && type != DataType.HASH) {
            redisTemplate.delete(key);
        }
    }

    // === 세션 관리 (HASH 타입) ===

    /**
     * 세션 저장
     * Redis Key: refresh:{userId}
     * Redis Type: HASH
     * Fields:
     *   - familyId: 세션군 ID
     *   - currentJti: 현재 유효한 Refresh Token의 JTI
     *   - rotatedAt: 마지막 회전 시각 (epoch ms)
     * TTL: Refresh Token 유효기간 (7일)
     */
    public void saveSession(String userId, String familyId, String currentJti,
                            long rotatedAtEpochMs, long ttlMs) {
        String key = keySession(userId);
        ensureHashKey(key);

        Map<String, String> sessionData = new HashMap<>(4);
        sessionData.put(FIELD_FAMILY_ID, familyId);
        sessionData.put(FIELD_CURRENT_JTI, currentJti);
        sessionData.put(FIELD_ROTATED_AT, String.valueOf(rotatedAtEpochMs));

        redisTemplate.opsForHash().putAll(key, sessionData);

        if (ttlMs > 0) {
            redisTemplate.expire(key, Duration.ofMillis(ttlMs));
        }
    }

    /**
     * 세션 조회
     */
    public RefreshSession findSession(String userId) {
        String key = keySession(userId);

        // 타입 검증: HASH가 아니면 null 반환
        DataType type = redisTemplate.type(key);
        if (type == null || type == DataType.NONE || type != DataType.HASH) {
            return null;
        }

        Object famObj = redisTemplate.opsForHash().get(key, FIELD_FAMILY_ID);
        if (famObj == null) {
            return null;
        }

        String familyId = Objects.toString(famObj, null);
        String currentJti = Objects.toString(
                redisTemplate.opsForHash().get(key, FIELD_CURRENT_JTI),
                null
        );

        String rotatedAtStr = Objects.toString(
                redisTemplate.opsForHash().get(key, FIELD_ROTATED_AT),
                "0"
        );

        long rotatedAt;
        try {
            rotatedAt = Long.parseLong(rotatedAtStr);
        } catch (NumberFormatException e) {
            rotatedAt = 0L;
        }

        return new RefreshSession(familyId, currentJti, rotatedAt);
    }

    /**
     * 세션 삭제
     */
    public void deleteSession(String userId) {
        redisTemplate.delete(keySession(userId));
    }

    // === 블랙리스트 관리 (String 타입) ===

    /**
     * JTI 블랙리스트 추가
     * Redis Key: blacklist:rt:{jti}
     * Redis Type: String
     * Value: "1"
     * TTL: 남은 토큰 유효시간
     *
     * 목적: 이미 사용된(회전된) Refresh Token 재사용 방지
     */
    public void blacklistJti(String jti, long ttlMs) {
        if (jti == null) {
            return;
        }

        String key = keyRtBlacklist(jti);

        if (ttlMs > 0) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttlMs));
        } else {
            redisTemplate.opsForValue().set(key, "1");
        }
    }

    /**
     * JTI 블랙리스트 확인
     */
    public boolean isJtiBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }

        Boolean exists = redisTemplate.hasKey(keyRtBlacklist(jti));
        return exists != null && exists;
    }

    /**
     * Family 블랙리스트 추가
     * Redis Key: blacklist:fam:{familyId}
     * Redis Type: String
     * Value: "1"
     * TTL: Refresh Token 유효기간
     *
     * 목적: 재사용 탐지 시 해당 세션군의 모든 토큰 무효화
     */
    public void blacklistFamily(String fam, long ttlMs) {
        if (fam == null) {
            return;
        }

        String key = keyFamBlacklist(fam);

        if (ttlMs > 0) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttlMs));
        } else {
            redisTemplate.opsForValue().set(key, "1");
        }
    }

    /**
     * Family 블랙리스트 확인
     */
    public boolean isFamilyBlacklisted(String fam) {
        if (fam == null) {
            return false;
        }

        Boolean exists = redisTemplate.hasKey(keyFamBlacklist(fam));
        return exists != null && exists;
    }
}