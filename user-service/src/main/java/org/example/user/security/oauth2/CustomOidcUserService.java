package org.example.user.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.example.shared.type.LoginType;
import org.example.user.domain.User;
import org.example.user.service.OAuth2UserRegistration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserRegistration registration;

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = super.loadUser(request);

        String providerId = oidcUser.getSubject();
        String email      = oidcUser.getAttribute("email");
        String nickname   = oidcUser.getAttribute("name");

        User user = registration.registerOrUpdate(
                LoginType.GOOGLE, providerId, email, nickname
        );

        List<GrantedAuthority> auths = List.copyOf(oidcUser.getAuthorities());
        OidcUserInfo userInfo        = new OidcUserInfo(oidcUser.getAttributes());
        String userNameAttributeName = "sub";

        return new DefaultOidcUser(auths, request.getIdToken(), userInfo, userNameAttributeName);
    }
}