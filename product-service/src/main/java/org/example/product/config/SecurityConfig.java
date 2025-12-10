package org.example.product.config;

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

        // Product Service 전용 권한 설정
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()     // 공통 인증 제외 경로
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()    // 상품 조회는 누구나 가능
                        .requestMatchers(HttpMethod.POST, "/api/products/**")               // 상품 등록/수정/삭제는 ADMIN만
                        .hasAuthority(RoleType.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasAuthority(RoleType.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                        .hasAuthority(RoleType.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasAuthority(RoleType.ROLE_ADMIN.name())
                        .anyRequest().authenticated()).build();
    }
}