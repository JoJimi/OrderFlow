package org.example.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.shared.type.common.LoginType;
import org.example.shared.type.common.RoleType;
import org.example.user.domain.User;

import java.time.LocalDateTime;

@Schema(description = "사용자 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "로그인 타입", example = "GOOGLE")
        LoginType loginType,

        @Schema(description = "사용자 역할", example = "ROLE_USER")
        RoleType role,

        @Schema(description = "생성 시간")
        LocalDateTime createdAt,

        @Schema(description = "수정 시간")
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}