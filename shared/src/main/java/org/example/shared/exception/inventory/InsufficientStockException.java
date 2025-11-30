package org.example.shared.exception.inventory;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}
