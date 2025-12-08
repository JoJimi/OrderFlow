package org.example.shared.security.userdetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.shared.security.jwt.JwtClaims;
import org.example.shared.type.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 공통 UserDetails 구현체
 * - 모든 서비스에서 사용 가능
 */
@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final String userId;
    private final RoleType role;
    private final Collection<? extends GrantedAuthority> authorities;

    public static SecurityUser from(JwtClaims claims) {
        RoleType role = RoleType.valueOf(claims.role());
        return new SecurityUser(
                claims.userId(),
                role,
                List.of(new SimpleGrantedAuthority(role.name()))
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