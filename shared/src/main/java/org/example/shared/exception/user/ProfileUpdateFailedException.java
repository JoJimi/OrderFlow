package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class ProfileUpdateFailedException extends BusinessException {
    public ProfileUpdateFailedException(ErrorCode errorCode) {
        super(ErrorCode.PROFILE_UPDATE_FAILED);
    }
}
