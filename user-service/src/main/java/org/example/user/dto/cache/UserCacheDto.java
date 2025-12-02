package org.example.user.dto.cache;

import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;

import java.io.Serializable;

/**
 * Redis 캐싱용 User DTO
 * - LocalDateTime 같은 복잡한 타입 제외
 * - 직렬화/역직렬화 최적화
 */
public record UserCacheDto(
        Long id,
        String email,
        String nickname,
        LoginType loginType,
        RoleType roleType,
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