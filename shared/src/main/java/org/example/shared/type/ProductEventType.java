package org.example.shared.type;

/**
 * 상품 이벤트 타입
 * Kafka를 통해 전파되는 상품 관련 이벤트의 종류를 정의합니다.
 */
public enum ProductEventType {
    // 상품 생성 이벤트
    PRODUCT_CREATED,

    // 상품 정보 수정 이벤트
    PRODUCT_UPDATED,

    // 상품 삭제 이벤트
    PRODUCT_DELETED,

    // 상품 대량 생성 이벤트
    PRODUCT_BULK_CREATED
}
