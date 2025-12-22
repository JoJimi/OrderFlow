package org.example.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

                // 현재 요청에 Authorization 헤더가 있다면 Feign 요청에도 그대로 실어 보냄
                if (authorizationHeader != null) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
                    log.debug("Feign 요청에 Authorization 헤더 추가함");
                }
            }
        };
    }
}