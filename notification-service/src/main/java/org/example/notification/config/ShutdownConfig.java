package org.example.notification.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.service.SseEmitterService;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 서버 종료 시 SSE 연결 정리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShutdownConfig {

    private final SseEmitterService sseEmitterService;

    @EventListener
    public void onApplicationShutdown(ContextClosedEvent event) {
        log.info("애플리케이션 종료 - SSE 연결 정리 시작");
        sseEmitterService.closeAll();
    }
}
