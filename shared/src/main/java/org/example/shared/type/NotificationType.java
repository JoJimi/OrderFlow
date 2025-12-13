package org.example.shared.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    ORDER("주문 알림", "주문 생성, 취소 등 주문 관련 알림"),
    PAYMENT("결제 알림", "결제 성공, 실패 등 결제 관련 알림"),
    SHIPPING("배송 알림", "배송 시작, 완료 등 배송 관련 알림"),
    ALERT("보안 알림", "비정상 로그인, 토큰 재사용 등 보안 관련 알림");

    private final String description;
    private final String detail;

    public static NotificationType fromCode(String code) {
        try {
            return NotificationType.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 알림 타입 코드: " + code);
        }
    }

    public boolean isSecurityAlert() {
        return this == ALERT;
    }

    public boolean isOrderRelated() {
        return this == ORDER || this == PAYMENT || this == SHIPPING;
    }
}