package org.example.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;

@Schema(description = "사용자 정보 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "로그인 타입", example = "KAKAO")
        LoginType loginType,

        @Schema(description = "권한", example = "USER")
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