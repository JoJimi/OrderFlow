package org.example.shared.type.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderEventType {
    ORDER_CREATED("주문 생성"),
    ORDER_CANCELLED("주문 취소"),
    ORDER_STATUS_CHANGED("주문 상태 변경");

    private final String description;
}