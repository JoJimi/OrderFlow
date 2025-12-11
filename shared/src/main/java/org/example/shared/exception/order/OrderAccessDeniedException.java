package org.example.shared.exception.order;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OrderAccessDeniedException extends BusinessException {
    public OrderAccessDeniedException() {
        super(ErrorCode.ORDER_ACCESS_DENIED);
    }
}
