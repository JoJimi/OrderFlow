package org.example.shared.exception.shipping;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ShippingAlreadyStartedException extends BusinessException {
    public ShippingAlreadyStartedException() {
        super(ErrorCode.SHIPPING_ALREADY_STARTED);
    }
}
