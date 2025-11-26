package org.example.shared.exception.common;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class MissingParameterException extends BusinessException {
    public MissingParameterException(ErrorCode errorCode) {
        super(ErrorCode.MISSING_REQUEST_PARAMETER);
    }
}
