package org.example.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.security.jwt.JwtTokenValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final BaseSecurityConfig baseSecurityConfig;
    private final JwtTokenValidator jwtTokenValidator;
    private final ObjectMapper objectMapper;

    /**
     * Notification-Service 전용 JwtAuthenticationFilter
     * BaseSecurityConfig의 빈 생성을 우회하고 명시적으로 null 주입
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        log.info("🔧 Notification-Service: JwtAuthenticationFilter 생성 (UserDetailsService=null, TokenBlacklistChecker=null)");

        return new JwtAuthenticationFilter(
                jwtTokenValidator,
                null,  // TokenBlacklistChecker - User Service만 사용
                objectMapper,
                null   // UserDetailsService - User Service만 사용
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter());

        return http
                .authorizeHttpRequests(auth -> auth
                        // 공통 인증 제외 경로 (Swagger, Actuator)
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()
                        // 알림 관련 모든 API는 인증 필요
                        .requestMatchers("/api/notifications/**").authenticated()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }
}