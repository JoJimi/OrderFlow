package org.example.user.dto.response;

import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        LoginType loginType,
        RoleType role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getRole()
        );
    }
}
