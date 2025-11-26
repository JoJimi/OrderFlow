package org.example.shared.exception.auth;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
