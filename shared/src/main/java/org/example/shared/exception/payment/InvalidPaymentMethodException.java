package org.example.shared.exception.payment;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidPaymentMethodException extends BusinessException {
    public InvalidPaymentMethodException() {
        super(ErrorCode.INVALID_PAYMENT_METHOD);
    }
}
