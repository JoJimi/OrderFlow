package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OAuthTokenInvalidException extends BusinessException {
    public OAuthTokenInvalidException(ErrorCode errorCode) {
        super(ErrorCode.OAUTH_TOKEN_INVALID);
    }
}
