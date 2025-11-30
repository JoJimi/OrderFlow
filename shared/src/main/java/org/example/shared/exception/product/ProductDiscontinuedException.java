package org.example.shared.exception.product;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ProductDiscontinuedException extends BusinessException {
    public ProductDiscontinuedException() {
        super(ErrorCode.PRODUCT_DISCONTINUED);
    }
}
