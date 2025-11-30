package org.example.shared.exception.common;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidTypeException extends BusinessException {
    public InvalidTypeException() {
        super(ErrorCode.INVALID_TYPE_VALUE);
    }
}
