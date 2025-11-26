package org.example.shared.exception.payment;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class PaymentFailedException extends BusinessException {
    public PaymentFailedException(ErrorCode errorCode) {
        super(ErrorCode.PAYMENT_FAILED);
    }
}
