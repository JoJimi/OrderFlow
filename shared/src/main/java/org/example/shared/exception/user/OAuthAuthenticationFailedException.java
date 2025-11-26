package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OAuthAuthenticationFailedException extends BusinessException {
    public OAuthAuthenticationFailedException(ErrorCode errorCode) {
        super(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
    }
}
