package org.example.user.dto.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.shared.type.common.LoginType;
import org.example.shared.type.common.RoleType;
import org.example.user.domain.User;

import java.io.Serializable;

/**
 * Redis 캐싱용 User DTO
 * - LocalDateTime 같은 복잡한 타입 제외
 * - 직렬화/역직렬화 최적화
 */
@Schema(description = "Redis 캐시용 사용자 정보")
public record UserCacheDto(
        @Schema(description = "사용자 ID")
        Long id,

        @Schema(description = "이메일")
        String email,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "로그인 타입")
        LoginType loginType,

        @Schema(description = "권한")
        RoleType roleType,

        @Schema(description = "OAuth Provider ID")
        String providerId
) implements Serializable {

    public static UserCacheDto from(User user) {
        return new UserCacheDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getRole(),
                user.getProviderId()
        );
    }
}