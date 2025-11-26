package org.example.exception.auth;

import org.example.exception.BusinessException;
import org.example.exception.ErrorCode;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
