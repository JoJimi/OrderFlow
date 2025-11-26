package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
