package org.example.shared.exception.notification;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidRecipientException extends BusinessException {
    public InvalidRecipientException(ErrorCode errorCode) {
        super(ErrorCode.INVALID_RECIPIENT);
    }
}
