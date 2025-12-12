package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 이벤트 타입 Enum
 * - Kafka 메시지의 이벤트 종류를 정의
 */
@Getter
@RequiredArgsConstructor
public enum OrderEventType {
    ORDER_CREATED("주문 생성"),
    ORDER_CANCELLED("주문 취소"),
    ORDER_STATUS_CHANGED("주문 상태 변경");

    private final String description;
}