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

        baseSecurityConfig.configureCommonSecurity(http, jwtAuthenticationFilter);

        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BaseSecurityConfig.COMMON_ALLOWLIST).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()
                        .anyRequest().authenticated()
                )
                .build();
    }
}