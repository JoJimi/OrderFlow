package org.example.shared.exception.product;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ProductOutOfStockException extends BusinessException {
    public ProductOutOfStockException(ErrorCode errorCode) {
        super(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }
}
