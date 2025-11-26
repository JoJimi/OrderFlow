package org.example.shared.exception.payment;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(ErrorCode errorCode) {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }
}
