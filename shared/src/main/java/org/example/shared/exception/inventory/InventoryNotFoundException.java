package org.example.shared.exception.inventory;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InventoryNotFoundException extends BusinessException {
    public InventoryNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.INVENTORY_NOT_FOUND);
    }
}
