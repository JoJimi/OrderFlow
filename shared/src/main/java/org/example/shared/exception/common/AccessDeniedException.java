package org.example.shared.exception.common;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
