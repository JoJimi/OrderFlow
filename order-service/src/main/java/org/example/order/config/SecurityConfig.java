package org.example.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.security.jwt.JwtTokenValidator;
import org.example.shared.type.common.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
     * Order-Service 전용 JwtAuthenticationFilter
     * BaseSecurityConfig의 빈 생성을 우회하고 명시적으로 null 주입
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        log.info("🔧 Order-Service: JwtAuthenticationFilter 생성 (UserDetailsService=null, TokenBlacklistChecker=null)");

        return new JwtAuthenticationFilter(
                jwtTokenValidator,
                null,
                objectMapper,
                null
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter());

        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()
                        .requestMatchers("/api/orders/**").authenticated()
                        .anyRequest().authenticated()
                )
                .build();
    }
}