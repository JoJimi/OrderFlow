package org.example.exception.auth;

import org.example.exception.BusinessException;
import org.example.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(ErrorCode errorCode) {
        super(ErrorCode.INVALID_TOKEN);
    }
}
