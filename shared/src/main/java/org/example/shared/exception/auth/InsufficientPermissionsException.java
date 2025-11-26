package org.example.shared.exception.auth;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InsufficientPermissionsException extends BusinessException {
    public InsufficientPermissionsException(ErrorCode errorCode) {
        super(ErrorCode.INSUFFICIENT_PERMISSIONS);
    }
}
