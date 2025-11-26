package org.example.shared.exception.notification;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class NotificationSendFailedException extends BusinessException {
    public NotificationSendFailedException(ErrorCode errorCode) {
        super(ErrorCode.NOTIFICATION_SEND_FAILED);
    }
}
