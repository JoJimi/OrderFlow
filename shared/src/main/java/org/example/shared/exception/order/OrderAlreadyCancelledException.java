package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderAlreadyCancelledException extends BusinessException {
    public OrderAlreadyCancelledException() {
        super(ErrorCode.ORDER_ALREADY_CANCELLED);
    }
}
