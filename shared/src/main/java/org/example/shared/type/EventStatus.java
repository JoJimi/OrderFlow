package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 이벤트 상태 Enum
 *
 * 상태 전이:
 * PENDING → SUCCESS
 * PENDING → FAILED
 */
@Getter
@RequiredArgsConstructor
public enum EventStatus {
    PENDING("처리 중", Set.of("SUCCESS", "FAILED")),
    SUCCESS("성공", Set.of()),
    FAILED("실패", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    /**
     * 다음 상태로 전환 가능한지 확인
     */
    public boolean canTransitionTo(EventStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    /**
     * 코드로부터 EventStatus 찾기
     */
    public static EventStatus fromCode(String code) {
        try {
            return EventStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 이벤트 상태 코드: " + code);
        }
    }

    /**
     * 완료 상태인지 확인 (SUCCESS 또는 FAILED)
     */
    public boolean isCompleted() {
        return this == SUCCESS || this == FAILED;
    }

    /**
     * 재시도 가능한 상태인지 확인
     */
    public boolean isRetryable() {
        return this == PENDING || this == FAILED;
    }
}