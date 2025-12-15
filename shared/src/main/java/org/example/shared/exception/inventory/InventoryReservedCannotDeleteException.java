package org.example.shared.exception.inventory;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InventoryReservedCannotDeleteException extends BusinessException {
    public InventoryReservedCannotDeleteException() {
        super(ErrorCode.INVENTORY_RESERVED_CANNOT_DELETE);
    }
}
