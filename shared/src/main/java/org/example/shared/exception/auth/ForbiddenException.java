package org.example.shared.exception.auth;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
