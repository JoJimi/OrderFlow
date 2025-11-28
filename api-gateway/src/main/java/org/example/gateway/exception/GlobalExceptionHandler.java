package org.example.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Gateway 전역 예외 처리 핸들러
 */
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            message = rse.getReason();
        }

        String errorResponse = createErrorResponse(status, message);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String createErrorResponse(HttpStatus status, String message) {
        return String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\",\"status\":%d}",
                status.getReasonPhrase(),
                message,
                LocalDateTime.now().toString(),
                status.value()
        );
    }
}