package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidOrderStatusException extends BusinessException {
    public InvalidOrderStatusException(ErrorCode errorCode) {
        super(ErrorCode.INVALID_ORDER_STATUS);
    }
}
