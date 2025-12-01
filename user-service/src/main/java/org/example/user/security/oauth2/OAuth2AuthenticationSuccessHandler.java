package org.example.user.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.type.LoginType;
import org.example.user.redis.TokenService;
import org.example.user.security.jwt.JwtTokenProvider;
import org.example.user.service.OAuth2UserRegistration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final int COOKIE_MAX_AGE = 180; // 3분 (토큰 교환용)
    private static final String TEMP_TOKEN_COOKIE_NAME = "temp_auth_token";

    private final JwtTokenProvider tokenProvider;
    private final TokenService tokenService;
    private final OAuth2UserRegistration registration;

    @Value("${app.frontBaseUrl}")
    private String frontBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        CustomOAuth2User oAuthUser = extractCustomOAuth2User(principal);

        String userId = oAuthUser.getUser().getId().toString();

        // 신규 세션군(family) 생성
        String familyId = tokenProvider.newFamilyId();

        // 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(userId, familyId);
        String refreshToken = tokenProvider.generateRefreshToken(userId, familyId);

        // Redis 세션 초기화
        String jti = tokenProvider.getJti(refreshToken);
        tokenService.initSession(userId, familyId, jti);

        // 보안 강화: HttpOnly 쿠키 또는 일회용 토큰 방식 권장
        // 현재는 쿼리스트링 방식 유지 (프론트엔드 호환성)
        redirectWithTokens(request, response, accessToken, refreshToken);
    }

    /**
     * CustomOAuth2User 추출 (카카오/구글 통합 처리)
     */
    private CustomOAuth2User extractCustomOAuth2User(OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser;
        }

        if (principal instanceof DefaultOidcUser oidcUser) {
            return new CustomOAuth2User(
                    registration.registerOrUpdate(
                            LoginType.GOOGLE,
                            oidcUser.getSubject(),
                            oidcUser.getAttribute("email"),
                            oidcUser.getAttribute("name")
                    ),
                    oidcUser.getAttributes(),
                    oidcUser.getAuthorities()
            );
        }

        throw new IllegalStateException("지원하지 않는 OAuth2User 타입: " + principal.getClass());
    }

    /**
     * 토큰과 함께 리다이렉트
     *
     * 보안 고려사항:
     * 1. 쿼리스트링: 브라우저 히스토리, 서버 로그에 노출 위험
     * 2. HttpOnly 쿠키: XSS 방어, 하지만 CSRF 공격 가능
     * 3. 일회용 교환 토큰: 가장 안전 (별도 엔드포인트에서 실제 토큰 교환)
     *
     * 운영 환경에서는 3번 방식 권장
     */
    private void redirectWithTokens(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String accessToken,
                                    String refreshToken) throws IOException {

        // TODO: 운영 환경에서는 아래 방식으로 변경 권장
        // redirectWithSecureCookie(response, accessToken, refreshToken);

        // 현재: 쿼리스트링 방식 (개발 편의성)
        String redirectUri = UriComponentsBuilder
                .fromHttpUrl(frontBaseUrl + "/oauth2/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("OAuth2 로그인 성공 리다이렉트: {}", frontBaseUrl + "/oauth2/callback");
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    /**
     * 보안 강화된 리다이렉트 (HttpOnly 쿠키 사용)
     * 프로덕션 환경 권장 방식
     */
    @SuppressWarnings("unused")
    private void redirectWithSecureCookie(HttpServletResponse response,
                                          String accessToken,
                                          String refreshToken) throws IOException {
        // Access Token을 HttpOnly 쿠키로 설정
        Cookie accessCookie = createSecureCookie("access_token", accessToken, 15 * 60); // 15분
        Cookie refreshCookie = createSecureCookie("refresh_token", refreshToken, 7 * 24 * 60 * 60); // 7일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // 토큰 없이 리다이렉트
        String redirectUri = frontBaseUrl + "/oauth2/callback";
        response.sendRedirect(redirectUri);
    }

    /**
     * 보안 쿠키 생성
     */
    private Cookie createSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // XSS 방어
        cookie.setSecure(true);     // HTTPS only
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict"); // CSRF 방어
        return cookie;
    }
}