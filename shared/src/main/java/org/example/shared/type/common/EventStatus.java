package org.example.shared.type.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
    PENDING("처리 중", Set.of("SUCCESS", "FAILED")),
    SUCCESS("성공", Set.of()),
    FAILED("실패", Set.of());

    private final String description;
    private final Set<String> allowedTransitions;

    public boolean canTransitionTo(EventStatus nextStatus) {
        return allowedTransitions.contains(nextStatus.name());
    }

    public static EventStatus fromCode(String code) {
        try {
            return EventStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 이벤트 상태 코드: " + code);
        }
    }

    public boolean isCompleted() {
        return this == SUCCESS || this == FAILED;
    }

    public boolean isRetryable() {
        return this == PENDING || this == FAILED;
    }
}