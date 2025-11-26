package org.example.shared.exception.payment;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class PaymentAlreadyCompletedException extends BusinessException {
    public PaymentAlreadyCompletedException(ErrorCode errorCode) {
        super(ErrorCode.PAYMENT_ALREADY_COMPLETED);
    }
}
