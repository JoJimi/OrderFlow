package org.example.shared.type.shipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShippingEventType {
    SHIPPING_PREPARING("배송 준비", "결제 완료 후 배송 준비 중"),
    SHIPPING_STARTED("배송 시작", "배송이 시작되었습니다"),
    SHIPPING_COMPLETED("배송 완료", "배송이 완료되었습니다");

    private final String description;
    private final String detail;
}