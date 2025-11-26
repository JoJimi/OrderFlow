package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderCannotBeCancelledException extends BusinessException {
    public OrderCannotBeCancelledException(ErrorCode errorCode) {
        super(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
    }
}
