package org.example.shared.util;

import de.huxhorn.sulky.ulid.ULID;

/**
 * 전역 ID 생성 유틸리티
 * - ULID 기반 (시간순 정렬 가능, 충돌 없음, 26자)
 * - 모든 마이크로서비스에서 공유
 */
public class IdGenerator {

    private static final ULID ulid = new ULID();

    private IdGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 주문 ID 생성
     * @return ORDER-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateOrderId() {
        return "ORDER-" + ulid.nextULID();
    }

    /**
     * 주문 항목 ID 생성
     * @return ITEM-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateOrderItemId() {
        return "ITEM-" + ulid.nextULID();
    }

    /**
     * 결제 ID 생성
     * @return PAY-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generatePaymentId() {
        return "PAY-" + ulid.nextULID();
    }

    /**
     * 재고 로그 ID 생성
     * @return INVLOG-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateInventoryLogId() {
        return "INVLOG-" + ulid.nextULID();
    }

    /**
     * 재고 ID 생성
     * @return INV-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateInventoryId() {
        return "INV-" + ulid.nextULID();
    }

    /**
     * 배송 ID 생성
     * @return SHIP-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateShipmentId() {
        return "SHIP-" + ulid.nextULID();
    }

    /**
     * 알림 ID 생성
     * @return NOTIF-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateNotificationId() {
        return "NOTIF-" + ulid.nextULID();
    }

    /**
     * 이벤트 로그 ID 생성
     * @return EVENT-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateEventLogId() {
        return "EVENT-" + ulid.nextULID();
    }

    /**
     * 범용 UUID 생성 (prefix 커스텀)
     * @param prefix 접두사 (예: "CUSTOM")
     * @return CUSTOM-01HQVX3K7N9XQJ5Y8W2S4P 형식
     */
    public static String generateId(String prefix) {
        return prefix + "-" + ulid.nextULID();
    }
}