package org.example.shared.exception.shipping;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidShippingAddressException extends BusinessException {
    public InvalidShippingAddressException() {
        super(ErrorCode.INVALID_SHIPPING_ADDRESS);
    }
}
