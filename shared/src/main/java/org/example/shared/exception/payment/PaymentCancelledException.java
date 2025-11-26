package org.example.shared.exception.payment;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class PaymentCancelledException extends BusinessException {
    public PaymentCancelledException(ErrorCode errorCode) {
        super(ErrorCode.PAYMENT_CANCELLED);
    }
}
