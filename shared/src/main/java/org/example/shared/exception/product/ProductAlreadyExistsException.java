package org.example.shared.exception.product;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ProductAlreadyExistsException extends BusinessException {
    public ProductAlreadyExistsException(ErrorCode errorCode) {
        super(ErrorCode.PRODUCT_ALREADY_EXISTS);
    }
}
