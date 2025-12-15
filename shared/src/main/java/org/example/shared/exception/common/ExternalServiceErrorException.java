package org.example.shared.exception.common;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ExternalServiceErrorException extends BusinessException {
    public ExternalServiceErrorException() {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}
