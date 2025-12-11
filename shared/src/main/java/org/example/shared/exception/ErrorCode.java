package org.example.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ============================================
    // 공통 에러 (CMN: Common) - 1000번대
    // ============================================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN001", "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CMN002", "잘못된 입력 값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "CMN003", "잘못된 타입입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "CMN004", "필수 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "CMN005", "지원하지 않는 HTTP 메서드입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "CMN006", "접근이 거부되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "CMN007", "요청한 리소스를 찾을 수 없습니다."),

    // ============================================
    // 인증/인가 에러 (AUTH) - 2000번대
    // ============================================
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH001", "인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH003", "만료된 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH004", "권한이 없습니다."),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTH005", "필요한 권한이 부족합니다."),

    // ============================================
    // 사용자 서비스 에러 (USER) - 3000번대
    // ============================================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "USER002", "비활성화된 사용자입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER003", "이미 존재하는 사용자입니다."),

    // OAuth2 관련 에러
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "USER004", "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "USER005", "OAuth 인증에 실패했습니다."),
    OAUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "USER006", "유효하지 않은 OAuth 토큰입니다."),
    OAUTH_USER_INFO_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER007", "OAuth 사용자 정보 조회에 실패했습니다."),
    OAUTH_PROVIDER_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "USER008", "OAuth 제공자 연결에 실패했습니다."),

    // 사용자 프로필 관련 에러
    INVALID_PROFILE_DATA(HttpStatus.BAD_REQUEST, "USER009", "유효하지 않은 프로필 정보입니다."),
    PROFILE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER010", "프로필 업데이트에 실패했습니다."),

    // ============================================
    // 상품 서비스 에러 (PROD: Product) - 4000번대
    // ============================================
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROD001", "상품을 찾을 수 없습니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PROD002", "재고가 부족합니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "PROD003", "유효하지 않은 상품 가격입니다."),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROD004", "이미 존재하는 상품입니다."),
    PRODUCT_DISCONTINUED(HttpStatus.BAD_REQUEST, "PROD005", "판매 중단된 상품입니다."),

    // ============================================
    // 주문 서비스 에러 (ORD: Order) - 5000번대
    // ============================================
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORD002", "유효하지 않은 주문 상태입니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "ORD003", "이미 취소된 주문입니다."),
    ORDER_CANNOT_BE_CANCELLED(HttpStatus.BAD_REQUEST, "ORD004", "취소할 수 없는 주문입니다."),
    EMPTY_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "ORD005", "주문 항목이 비어있습니다."),
    ORDER_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "ORD006", "주문 금액이 일치하지 않습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "ORD007", "본인 주문이 아닙니다."),

    // ============================================
    // 결제 서비스 에러 (PAY: Payment) - 6000번대
    // ============================================
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAY002", "결제에 실패했습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "PAY003", "이미 완료된 결제입니다."),
    PAYMENT_CANCELLED(HttpStatus.BAD_REQUEST, "PAY004", "취소된 결제입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "PAY005", "잔액이 부족합니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PAY006", "유효하지 않은 결제 수단입니다."),

    // ============================================
    // 재고 서비스 에러 (INV: Inventory) - 7000번대
    // ============================================
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INV001", "재고 정보를 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INV002", "재고가 부족합니다."),
    INVENTORY_LOCK_FAILED(HttpStatus.CONFLICT, "INV003", "재고 잠금에 실패했습니다."),
    NEGATIVE_STOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "INV004", "재고는 음수가 될 수 없습니다."),

    // ============================================
    // 배송 서비스 에러 (SHIP: Shipping) - 8000번대
    // ============================================
    SHIPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "SHIP001", "배송 정보를 찾을 수 없습니다."),
    INVALID_SHIPPING_ADDRESS(HttpStatus.BAD_REQUEST, "SHIP002", "유효하지 않은 배송 주소입니다."),
    SHIPPING_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "SHIP003", "이미 배송이 시작되었습니다."),
    SHIPPING_CANCELLED(HttpStatus.BAD_REQUEST, "SHIP004", "취소된 배송입니다."),

    // ============================================
    // 알림 서비스 에러 (NOT: Notification) - 9000번대
    // ============================================
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOT001", "알림 전송에 실패했습니다."),
    INVALID_NOTIFICATION_TEMPLATE(HttpStatus.BAD_REQUEST, "NOT002", "유효하지 않은 알림 템플릿입니다."),
    INVALID_RECIPIENT(HttpStatus.BAD_REQUEST, "NOT003", "유효하지 않은 수신자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
