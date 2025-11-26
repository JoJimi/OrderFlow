package org.example.exception.auth;

import org.example.exception.BusinessException;
import org.example.exception.ErrorCode;

public class ExpiredTokenException extends BusinessException {
    public ExpiredTokenException(ErrorCode errorCode) {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}
