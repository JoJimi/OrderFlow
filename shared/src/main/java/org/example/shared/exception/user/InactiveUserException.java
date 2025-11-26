package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InactiveUserException extends BusinessException {
    public InactiveUserException(ErrorCode errorCode) {
        super(ErrorCode.INACTIVE_USER);
    }
}
