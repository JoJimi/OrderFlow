package org.example.notification.service.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE Emitter 관리 서비스
 * 각 서버 인스턴스에서 자신에게 연결된 클라이언트만 관리
 */
@Service
@Slf4j
public class SseEmitterService {

    // userId -> SseEmitter 매핑 (이 서버 인스턴스에 연결된 클라이언트만)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 타임아웃 (30분)
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 클라이언트 SSE 연결 생성
     */
    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 기존 연결이 있으면 종료 (동일 사용자의 중복 연결 방지)
        SseEmitter oldEmitter = emitters.get(userId);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
            } catch (Exception e) {
                log.warn("기존 SSE 연결 종료 실패 - userId: {}", userId);
            }
        }

        // 새 연결 저장
        emitters.put(userId, emitter);
        log.info("✅ SSE 연결 생성 - userId: {}, 총 연결 수: {}", userId, emitters.size());

        // 연결 종료 이벤트 핸들러
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 완료 - userId: {}, 남은 연결 수: {}", userId, emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 타임아웃 - userId: {}, 남은 연결 수: {}", userId, emitters.size());
        });

        emitter.onError((e) -> {
            emitters.remove(userId);
            log.error("SSE 연결 오류 - userId: {}", userId, e);
        });

        // 연결 확인용 초기 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패 - userId: {}", userId, e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     * 이 서버에 연결되어 있는 경우에만 전송
     */
    public void sendToClient(String userId, Object data) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.debug("SSE 연결 없음 (다른 서버에 연결되어 있을 수 있음) - userId: {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data));
            log.info("📢 SSE 알림 전송 성공 - userId: {}", userId);
        } catch (IOException e) {
            log.error("SSE 알림 전송 실패 - userId: {}", userId, e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }

    /**
     * 연결된 사용자 확인
     */
    public boolean isConnected(String userId) {
        return emitters.containsKey(userId);
    }

    /**
     * 현재 연결 수
     */
    public int getConnectionCount() {
        return emitters.size();
    }

    /**
     * 모든 연결 종료 (서버 종료 시)
     */
    public void closeAll() {
        log.info("모든 SSE 연결 종료 - 총 {}개", emitters.size());
        emitters.values().forEach(emitter -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("SSE 연결 종료 실패", e);
            }
        });
        emitters.clear();
    }
}