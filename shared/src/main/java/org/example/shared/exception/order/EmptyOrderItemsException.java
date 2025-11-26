package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class EmptyOrderItemsException extends BusinessException {
    public EmptyOrderItemsException(ErrorCode errorCode) {
        super(ErrorCode.EMPTY_ORDER_ITEMS);
    }
}
