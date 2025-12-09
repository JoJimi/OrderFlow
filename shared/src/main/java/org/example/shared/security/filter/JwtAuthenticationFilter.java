package org.example.shared.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.exception.ErrorCode;
import org.example.shared.exception.ErrorResponse;
import org.example.shared.security.jwt.JwtClaims;
import org.example.shared.security.jwt.JwtTokenValidator;
import org.example.shared.security.service.TokenBlacklistChecker;
import org.example.shared.security.service.UserDetailsLoader;
import org.example.shared.security.userdetails.SecurityUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String TOKEN_PARAM = "token";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtTokenValidator tokenValidator;
    private final TokenBlacklistChecker blacklistChecker;  // nullable
    private final ObjectMapper objectMapper;
    private final UserDetailsLoader userDetailsLoader;  // nullable

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("=== JWT Filter 시작: {} ===", requestUri);

        if (isOptionsRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            log.debug("토큰 발견: {}...", token.substring(0, Math.min(20, token.length())));
            try {
                authenticateWithToken(token);
            } catch (ExpiredJwtException e) {
                log.warn("만료된 JWT 토큰: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
                return;
            } catch (JwtException e) {
                log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            } catch (Exception e) {
                log.error("인증 필터 오류", e);
                sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
                return;
            }
        } else {
            log.debug("토큰 없음 - 익명 요청");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOptionsRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void authenticateWithToken(String token) {
        log.debug(">>> authenticateWithToken 시작");

        // 1. 토큰 검증
        if (!tokenValidator.validateAccessToken(token)) {
            log.warn("토큰 검증 실패");
            return;
        }
        log.debug("✓ 토큰 검증 성공");

        // 2. Claims 추출
        JwtClaims claims = tokenValidator.extractClaims(token);
        log.info("===== JWT Claims =====");
        log.info("userId: {}", claims.userId());
        log.info("role: {}", claims.role());
        log.info("familyId: {}", claims.familyId());
        log.info("jti: {}", claims.jti());
        log.info("=====================");

        // 3. 블랙리스트 확인 (user-service만 해당)
        if (blacklistChecker != null &&
                blacklistChecker.isBlacklisted(claims.jti(), claims.familyId())) {
            log.warn("블랙리스트된 토큰 사용 시도: jti={}, familyId={}",
                    claims.jti(), claims.familyId());
            return;
        }
        log.debug("✓ 블랙리스트 검증 통과");

        // 4. 이미 인증되어 있는지 확인
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("이미 인증됨 - 스킵");
            return;
        }

        // 5. UserDetails 생성
        UserDetails userDetails = createUserDetails(claims);

        log.info("===== UserDetails 로드 완료 =====");
        log.info("username: {}", userDetails.getUsername());
        log.info("authorities: {}", userDetails.getAuthorities());
        userDetails.getAuthorities().forEach(auth ->
                log.info("  - Authority: {}", auth.getAuthority())
        );
        log.info("================================");

        // 6. Authentication 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        log.info("===== Authentication 생성 =====");
        log.info("Principal: {}", authentication.getPrincipal());
        log.info("Authorities: {}", authentication.getAuthorities());
        log.info("==============================");

        // 7. SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("✓✓✓ SecurityContext에 인증 정보 설정 완료 ✓✓✓");
    }

    /**
     * UserDetails 생성
     * - userDetailsLoader가 있으면 DB에서 조회 (user-service)
     * - userDetailsLoader가 없으면 SecurityUser 생성 (다른 서비스)
     */
    private UserDetails createUserDetails(JwtClaims claims) {
        if (userDetailsLoader != null) {
            // user-service: DB에서 실제 사용자 정보 조회
            log.debug("DB에서 사용자 정보 조회 (userDetailsLoader 사용)");
            return userDetailsLoader.loadUserByUserId(claims.userId());
        } else {
            // 다른 서비스: 경량 SecurityUser 생성
            log.debug("경량 SecurityUser 생성 (DB 조회 없음)");
            return new SecurityUser(
                    claims.userId(),
                    claims.role(),
                    Collections.singletonList(new SimpleGrantedAuthority(claims.role()))
            );
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of(code, "");
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        String tokenFromQuery = extractFromQueryParameter(request);
        if (StringUtils.hasText(tokenFromQuery)) {
            return tokenFromQuery;
        }

        return extractFromCookie(request);
    }

    private String extractFromQueryParameter(HttpServletRequest request) {
        String token = request.getParameter(ACCESS_TOKEN_PARAM);
        if (!StringUtils.hasText(token)) {
            token = request.getParameter(TOKEN_PARAM);
        }
        return StringUtils.hasText(token) ? token.trim() : null;
    }

    private String extractFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (StringUtils.hasText(value)) {
                    return value.trim();
                }
            }

            if (AUTHORIZATION_HEADER.equalsIgnoreCase(cookie.getName())) {
                String value = cookie.getValue();
                if (StringUtils.hasText(value) && value.startsWith(BEARER_PREFIX)) {
                    return value.substring(BEARER_PREFIX.length()).trim();
                }
            }
        }

        return null;
    }
}