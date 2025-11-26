package org.example.shared.exception.common;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class MethodNotAllowedException extends BusinessException {
    public MethodNotAllowedException(ErrorCode errorCode) {
        super(ErrorCode.METHOD_NOT_ALLOWED);
    }
}
