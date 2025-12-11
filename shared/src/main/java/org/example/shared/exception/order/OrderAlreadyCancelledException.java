package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderAlreadyCancelledException extends BusinessException {
    public OrderAlreadyCancelledException() {
        super(ErrorCode.ORDER_ALREADY_CANCELLED);
    }

    public OrderAlreadyCancelledException(String orderId) {
        super(ErrorCode.ORDER_ALREADY_CANCELLED, String.format("이미 취소된 주문입니다. orderId: %s", orderId));
    }
}
