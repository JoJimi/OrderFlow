package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }

    public OrderNotFoundException(String orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, String.format("주문을 찾을 수 없습니다. orderId: %s", orderId));
    }
}
