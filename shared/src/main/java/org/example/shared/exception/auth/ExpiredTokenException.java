package org.example.shared.exception.auth;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ExpiredTokenException extends BusinessException {
    public ExpiredTokenException(ErrorCode errorCode) {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}
