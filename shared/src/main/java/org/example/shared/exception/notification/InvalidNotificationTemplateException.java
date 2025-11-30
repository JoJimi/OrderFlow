package org.example.shared.exception.notification;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidNotificationTemplateException extends BusinessException {
    public InvalidNotificationTemplateException() {
        super(ErrorCode.INVALID_NOTIFICATION_TEMPLATE);
    }
}
