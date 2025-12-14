package org.example.shared.security.jwt;

import org.example.shared.type.common.RoleType;

import java.util.Date;

/**
 * JWT Claims DTO
 */
public record JwtClaims(
        String userId,
        String role,
        String familyId,
        String jti,
        Date expiration
) {
    public boolean hasRole(RoleType roleType) {
        return roleType.name().equals(role);
    }

    public boolean isAdmin() {
        return hasRole(RoleType.ROLE_ADMIN);
    }

    public boolean isUser() {
        return hasRole(RoleType.ROLE_USER);
    }
}