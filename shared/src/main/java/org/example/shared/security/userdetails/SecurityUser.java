package org.example.shared.security.userdetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.shared.security.jwt.JwtClaims;
import org.example.shared.type.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final String userId;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public static SecurityUser from(JwtClaims claims) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(claims.role());

        return new SecurityUser(
                claims.userId(),
                claims.role(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}