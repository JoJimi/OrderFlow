package org.example.shared.exception.user;

import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;

public class OAuthUserInfoFetchFailedException extends BusinessException {
    public OAuthUserInfoFetchFailedException() {
        super(ErrorCode.OAUTH_USER_INFO_FETCH_FAILED);
    }
}
