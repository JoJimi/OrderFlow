package org.example.shared.security.config;

import org.example.shared.security.handler.JwtAccessDeniedHandler;
import org.example.shared.security.handler.JwtAuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 공통 Security 설정 유틸리티
 */
public class BaseSecurityConfig {

    /**
     * 공통 CORS 설정
     */
    public static CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "X-Requested-With"));
        corsConfiguration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    /**
     * 공통 예외 핸들러 설정
     */
    public static void configureExceptionHandlers(
            HttpSecurity http,
            JwtAuthenticationEntryPoint entryPoint,
            JwtAccessDeniedHandler accessDeniedHandler) throws Exception {

        http.exceptionHandling(ex -> {
            ex.authenticationEntryPoint(entryPoint);
            ex.accessDeniedHandler(accessDeniedHandler);
        });
    }
}