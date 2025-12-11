package org.example.shared.exception.inventory;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class NegativeStockNotAllowedException extends BusinessException {
    public NegativeStockNotAllowedException() {
        super(ErrorCode.NEGATIVE_STOCK_NOT_ALLOWED);
    }

    public NegativeStockNotAllowedException(String message) {
        super(ErrorCode.NEGATIVE_STOCK_NOT_ALLOWED, message);
    }
}
