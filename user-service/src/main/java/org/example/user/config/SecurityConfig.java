package org.example.user.config;

import lombok.RequiredArgsConstructor;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.type.common.RoleType;
import org.example.user.security.oauth2.CustomOAuth2UserService;
import org.example.user.security.oauth2.CustomOidcUserService;
import org.example.user.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final BaseSecurityConfig baseSecurityConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // OAuth2 관련
    private final OAuth2AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService oauth2Service;
    private final CustomOidcUserService oidcService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 공통 설정 적용
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter);

        // User Service 전용 설정
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()       // 공통 인증 제외 경로
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()      // OAuth2 경로
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()    // 토큰 갱신
                        .requestMatchers("/api/performance/**").permitAll()                   // 성능 테스트

                        .requestMatchers(HttpMethod.DELETE, "/api/auth/logout")
                        .hasAuthority(RoleType.ROLE_USER.name())    // ROLE_USER 권한 필요 (ROLE_ADMIN은 역할 계층으로 자동 포함)
                        .requestMatchers("/admin/**")
                        .hasAuthority(RoleType.ROLE_ADMIN.name())   // ROLE_ADMIN만 접근 가능
                        .requestMatchers("/api/**")
                        .hasAuthority(RoleType.ROLE_USER.name())    // ROLE_USER 권한 필요
                        .anyRequest().authenticated())

                .oauth2Login(oauth2 -> oauth2                       // OAuth2 로그인 설정 (user-service만)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2Service)
                                .oidcUserService(oidcService)
                        ).successHandler(oAuth2LoginSuccessHandler)).build();
    }

    @Bean
    public static DefaultOAuth2UserService defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }
}