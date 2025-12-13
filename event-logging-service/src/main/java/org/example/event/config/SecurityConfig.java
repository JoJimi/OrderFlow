package org.example.event.config;

import lombok.RequiredArgsConstructor;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final BaseSecurityConfig baseSecurityConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 공통 설정 적용
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter);

        // EventLogging Service 전용 권한 설정
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()       // 공통 인증 제외 경로
                        .requestMatchers(HttpMethod.GET, "/api/events").authenticated()       // 전체 조회는 @PreAuthorize로 제어
                        .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()    // 주문별 조회
                        .anyRequest().authenticated()
                )
                .build();
    }
}