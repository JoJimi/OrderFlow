package org.example.shared.exception.shipping;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ShippingNotFoundException extends BusinessException {
    public ShippingNotFoundException() {
        super(ErrorCode.SHIPPING_NOT_FOUND);
    }
}
