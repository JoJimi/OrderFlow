package org.example.shared.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.security.filter.JwtAuthenticationFilter;
import org.example.shared.security.handler.JwtAccessDeniedHandler;
import org.example.shared.security.handler.JwtAuthenticationEntryPoint;
import org.example.shared.security.jwt.JwtTokenValidator;
import org.example.shared.security.service.TokenBlacklistChecker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

/**
 * 모든 마이크로서비스에서 공통으로 사용하는 Base Security 설정
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class BaseSecurityConfig {

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final ObjectMapper objectMapper;

    /**
     * 공통 인증 제외 경로
     */
    public static final String[] COMMON_ALLOWLIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**",
            "/actuator/health/**"
    };

    /**
     * JwtAuthenticationFilter 빈 생성
     * - Optional을 사용하여 선택적 의존성 처리
     * - TokenBlacklistChecker: user-service만 제공
     * - UserDetailsService: user-service만 제공
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            Optional<TokenBlacklistChecker> blacklistChecker,
            Optional<UserDetailsService> userDetailsService) {

        log.info("🔧 JwtAuthenticationFilter 빈 생성");
        log.info("  - TokenBlacklistChecker: {}", blacklistChecker.isPresent() ? "있음" : "없음");
        log.info("  - UserDetailsService: {}", userDetailsService.isPresent() ? "있음" : "없음");

        return new JwtAuthenticationFilter(
                jwtTokenValidator,
                blacklistChecker.orElse(null),
                objectMapper,
                userDetailsService.orElse(null)
        );
    }

    /**
     * SecurityContext를 세션에 저장하지 않도록 설정
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityContextRepository nullSecurityContextRepository() {
        return new NullSecurityContextRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
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
     * 공통 HttpSecurity 설정 헬퍼 메서드
     */
    public void configureCommonSecurity(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(nullSecurityContextRepository()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(x -> {
                    x.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                    x.accessDeniedHandler(jwtAccessDeniedHandler);
                });
    }
}