package org.example.payment.exception;

import lombok.Getter;
import org.example.payment.dto.toss.TossErrorResponse;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

/**
 * Toss Payments API 호출 중 발생하는 예외
 */
@Getter
public class TossPaymentException extends BusinessException {

    private final String tossErrorCode;
    private final String tossErrorMessage;

    public TossPaymentException(TossErrorResponse error) {
        super(ErrorCode.PAYMENT_FAILED, error.message());
        this.tossErrorCode = error.code();
        this.tossErrorMessage = error.message();
    }

    public TossPaymentException(String code, String message) {
        super(ErrorCode.PAYMENT_FAILED, message);
        this.tossErrorCode = code;
        this.tossErrorMessage = message;
    }

    /**
     * 재시도 가능한 에러인지 확인
     */
    public boolean isRetryable() {
        return tossErrorCode != null && (
                tossErrorCode.startsWith("PROVIDER_") ||
                        "FAILED_INTERNAL_SYSTEM_PROCESSING".equals(tossErrorCode)
        );
    }

    /**
     * 사용자에게 친절한 에러 메시지 반환
     */
    public String getUserFriendlyMessage() {
        if (tossErrorCode == null) {
            return "결제 처리 중 오류가 발생했습니다.";
        }

        return switch (tossErrorCode) {
            case "REJECT_CARD_PAYMENT" -> "카드 결제가 거부되었습니다. 카드사에 문의해주세요.";
            case "INSUFFICIENT_BALANCE" -> "잔액이 부족합니다.";
            case "INVALID_CARD_NUMBER" -> "카드 번호가 올바르지 않습니다.";
            case "INVALID_CARD_EXPIRATION" -> "카드 유효기간이 올바르지 않습니다.";
            case "EXCEED_MAX_DAILY_PAYMENT_COUNT" -> "일일 결제 한도를 초과했습니다.";
            case "EXCEED_MAX_PAYMENT_AMOUNT" -> "결제 한도를 초과했습니다.";
            case "NOT_FOUND_PAYMENT" -> "결제 정보를 찾을 수 없습니다.";
            case "ALREADY_PROCESSED_PAYMENT" -> "이미 처리된 결제입니다.";
            case "PROVIDER_ERROR" -> "결제사 연동 오류입니다. 잠시 후 다시 시도해주세요.";
            default -> tossErrorMessage != null ? tossErrorMessage : "결제 처리 중 오류가 발생했습니다.";
        };
    }
}