package org.example.exception.auth;

import org.example.exception.BusinessException;
import org.example.exception.ErrorCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
