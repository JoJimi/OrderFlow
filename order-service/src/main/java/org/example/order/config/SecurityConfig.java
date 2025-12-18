package org.example.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.shared.security.config.BaseSecurityConfig;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.security.jwt.JwtTokenValidator;
import org.example.shared.type.common.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final BaseSecurityConfig baseSecurityConfig;
    private final JwtTokenValidator jwtTokenValidator;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenValidator, null, objectMapper, null);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter());

        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()
                        .requestMatchers( "/api/orders/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}