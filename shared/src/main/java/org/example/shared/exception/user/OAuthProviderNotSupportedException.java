package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OAuthProviderNotSupportedException extends BusinessException {
    public OAuthProviderNotSupportedException(ErrorCode errorCode) {
        super(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
    }
}
