package org.example.order.config;

import lombok.RequiredArgsConstructor;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.type.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final BaseSecurityConfig baseSecurityConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 공통 설정 적용
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter);

        // Order Service 전용 권한 설정
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()     // 공통 인증 제외 경로
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAuthority(RoleType.ROLE_USER.name())           // 주문 생성
                        .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()                                    // 주문 목록 (본인 or ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority(RoleType.ROLE_USER.name())        // 주문 상세
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/**/cancel").hasAuthority(RoleType.ROLE_USER.name()) // 주문 취소
                        .anyRequest().authenticated()
                )
                .build();
    }
}