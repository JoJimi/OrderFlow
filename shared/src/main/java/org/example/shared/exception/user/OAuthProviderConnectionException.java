package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OAuthProviderConnectionException extends BusinessException {
    public OAuthProviderConnectionException() {
        super(ErrorCode.OAUTH_PROVIDER_CONNECTION_ERROR);
    }
}
