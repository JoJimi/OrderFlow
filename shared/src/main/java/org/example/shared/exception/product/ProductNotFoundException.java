package org.example.shared.exception.product;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
