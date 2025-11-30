package org.example.shared.exception.product;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidProductPriceException extends BusinessException {
    public InvalidProductPriceException() {
        super(ErrorCode.INVALID_PRODUCT_PRICE);
    }
}
