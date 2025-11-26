package org.example.shared.exception.inventory;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InventoryLockFailedException extends BusinessException {
    public InventoryLockFailedException(ErrorCode errorCode) {
        super(ErrorCode.INVENTORY_LOCK_FAILED);
    }
}
