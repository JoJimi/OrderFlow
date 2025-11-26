package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}
