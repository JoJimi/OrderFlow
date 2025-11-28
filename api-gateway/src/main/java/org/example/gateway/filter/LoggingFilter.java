package org.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 요청/응답 로깅 및 Request ID 추가 필터
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();

        log.info("Request ID: {} | Method: {} | Path: {} | Remote Address: {}",
                requestId,
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Request ID: {} | Status: {} | Duration: {}ms",
                            requestId,
                            exchange.getResponse().getStatusCode(),
                            duration);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}