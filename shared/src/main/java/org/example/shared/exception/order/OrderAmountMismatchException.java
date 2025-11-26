package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderAmountMismatchException extends BusinessException {
    public OrderAmountMismatchException(ErrorCode errorCode) {
        super(ErrorCode.ORDER_AMOUNT_MISMATCH);
    }
}
