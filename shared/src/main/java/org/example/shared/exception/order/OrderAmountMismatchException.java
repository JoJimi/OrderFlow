package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderAmountMismatchException extends BusinessException {
    public OrderAmountMismatchException() {
        super(ErrorCode.ORDER_AMOUNT_MISMATCH);
    }

    public OrderAmountMismatchException(String message) {
        super(ErrorCode.ORDER_AMOUNT_MISMATCH, message);
    }
}
