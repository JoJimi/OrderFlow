package org.example.shared.exception.shipping;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ShippingCancelledException extends BusinessException {
    public ShippingCancelledException() {
        super(ErrorCode.SHIPPING_CANCELLED);
    }
}
