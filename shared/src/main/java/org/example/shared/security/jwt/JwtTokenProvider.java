package org.example.shared.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements InitializingBean {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String FAMILY_ID_CLAIM = "fam";
    private static final String JTI_CLAIM = "jti";
    private static final String ACCESS_TOKEN_TYPE = "accessToken";
    private static final String REFRESH_TOKEN_TYPE = "refreshToken";

    private final JwtProperties jwtProperties;
    private Key key;

    @Override
    public void afterPropertiesSet() {
        String raw = jwtProperties.getJwtSecretKey();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(raw);
        } catch (IllegalArgumentException e) {
            keyBytes = HexFormat.of().parseHex(raw);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     * - 유효기간: 15분
     * - 포함 클레임: userId(subject), token_type, familyId
     */
    public String generateAccessToken(String userId, String familyId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userId)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(FAMILY_ID_CLAIM, familyId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     * - 유효기간: 7일
     * - 포함 클레임: userId(subject), token_type, familyId, jti(토큰 고유 ID)
     */
    public String generateRefreshToken(String userId, String familyId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userId)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .claim(FAMILY_ID_CLAIM, familyId)
                .claim(JTI_CLAIM, UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

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
     * - 서명 검증
     * - 만료 확인
     * - 토큰 타입 확인
     */
    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
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
     * 토큰에서 사용자 ID(subject) 추출
     */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 Family ID 추출
     */
    public String getFamilyId(String token) {
        return parseClaims(token).get(FAMILY_ID_CLAIM, String.class);
    }

    /**
     * 토큰에서 JTI(JWT ID) 추출
     */
    public String getJti(String token) {
        return parseClaims(token).get(JTI_CLAIM, String.class);
    }

    /**
     * 토큰 만료 시간 추출
     */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * 새로운 Family ID 생성
     */
    public String newFamilyId() {
        return UUID.randomUUID().toString();
    }

    /**
     * JWT 파싱 (public으로 변경)
     * - 서명 검증 + Claims 추출
     * - 실패 시 예외 발생
     */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}