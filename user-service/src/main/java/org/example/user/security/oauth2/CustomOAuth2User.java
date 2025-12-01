package org.example.user.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final User user;                             // 우리 서비스의 사용자 엔티티
    private final Map<String, Object> attributes;        // OAuth2 attributes
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getName() {
        // Authentication의 principal 식별자 - 여기서는 이메일 또는 "provider|id" 등을 사용할 수 있음
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
