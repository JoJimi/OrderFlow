package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(ErrorCode errorCode) {
        super(ErrorCode.USER_ALREADY_EXISTS);
    }
}
