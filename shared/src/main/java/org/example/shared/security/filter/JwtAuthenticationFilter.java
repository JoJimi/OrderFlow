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
import org.example.shared.security.userdetails.SecurityUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String TOKEN_PARAM = "token";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtTokenValidator tokenValidator;
    private final TokenBlacklistChecker blacklistChecker;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isOptionsRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
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
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOptionsRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void authenticateWithToken(String token) {
        if (!tokenValidator.validateAccessToken(token)) {
            return;
        }

        JwtClaims claims = tokenValidator.extractClaims(token);

        // 블랙리스트 확인 (선택적)
        if (blacklistChecker != null &&
                blacklistChecker.isBlacklisted(claims.jti(), claims.familyId())) {
            log.warn("블랙리스트된 토큰 사용 시도: jti={}, familyId={}",
                    claims.jti(), claims.familyId());
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        SecurityUser user = SecurityUser.from(claims);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
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