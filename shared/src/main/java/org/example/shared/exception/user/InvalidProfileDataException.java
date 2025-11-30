package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class InvalidProfileDataException extends BusinessException {
    public InvalidProfileDataException() {
        super(ErrorCode.INVALID_PROFILE_DATA);
    }
}
