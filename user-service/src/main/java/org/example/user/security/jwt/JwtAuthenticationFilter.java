package org.example.user.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.exception.ErrorCode;
import org.example.shared.exception.ErrorResponse;
import org.example.user.redis.TokenService;
import org.example.user.security.userdetails.CustomUserDetailsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String TOKEN_PARAM = "token";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // OPTIONS 요청은 인증 없이 통과
        if (isOptionsRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String token = resolveToken(request);

        // 토큰이 있으면 인증 처리
        if (StringUtils.hasText(token)) {
            try {
                authenticateWithToken(token);
            } catch (ExpiredJwtException e) {
                log.warn("만료된 JWT 토큰: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                return;
            } catch (JwtException e) {
                log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                return;
            } catch (Exception e) {
                log.error("인증 필터 오류", e);
                sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * OPTIONS 요청 확인
     */
    private boolean isOptionsRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /**
     * 토큰 기반 인증 처리
     */
    private void authenticateWithToken(String token) {
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateAccessToken(token)) {
            return;
        }

        String userId = jwtTokenProvider.getSubject(token);
        String familyId = jwtTokenProvider.getFamilyId(token);
        String jti = jwtTokenProvider.getJti(token);  // JTI 추출

        // JTI 블랙리스트 체크 (로그아웃된 토큰)
        if (tokenService.isJtiBlacklisted(jti)) {
            log.warn("블랙리스트된 Access Token 사용 시도: {}", jti);
            return;
        }

        // Family 블랙리스트 체크 (재사용 탐지 후 세션군 차단)
        if (tokenService.isFamilyBlacklisted(familyId)) {
            log.warn("차단된 세션군(familyId)의 토큰 사용 시도: {}", familyId);
            return;
        }

        // 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        // UserDetails 로드 및 인증 객체 생성
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of(code, "");
        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * 요청에서 토큰 추출
     * 우선순위: 1. Authorization 헤더 > 2. 쿼리 파라미터 > 3. 쿠키
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. 쿼리 파라미터에서 추출 (웹소켓 등 특수 케이스)
        String tokenFromQuery = extractFromQueryParameter(request);
        if (StringUtils.hasText(tokenFromQuery)) {
            return tokenFromQuery;
        }

        // 3. 쿠키에서 추출
        return extractFromCookie(request);
    }

    /**
     * 쿼리 파라미터에서 토큰 추출
     */
    private String extractFromQueryParameter(HttpServletRequest request) {
        String token = request.getParameter(ACCESS_TOKEN_PARAM);
        if (!StringUtils.hasText(token)) {
            token = request.getParameter(TOKEN_PARAM);
        }
        return StringUtils.hasText(token) ? token.trim() : null;
    }

    /**
     * 쿠키에서 토큰 추출
     */
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