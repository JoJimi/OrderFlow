package org.example.user.service;

import lombok.RequiredArgsConstructor;
import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserRegistration {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 등록 또는 업데이트
     * - 기존 사용자: 프로필 업데이트 (변경 시에만 저장)
     * - 신규 사용자: 생성 후 저장
     */
    public User registerOrUpdate(LoginType loginType, String providerId,
                                 String email, String nickname) {
        return userRepository.findByLoginTypeAndProviderId(loginType, providerId)
                .map(existing -> updateIfChanged(existing, email, nickname))
                .orElseGet(() -> createNewUser(loginType, providerId, email, nickname));
    }

    /**
     * 기존 사용자 프로필 업데이트 (변경 사항이 있을 때만 저장)
     */
    private User updateIfChanged(User user, String email, String nickname) {
        boolean changed = user.updateProfile(email, nickname);
        return changed ? userRepository.save(user) : user;
    }

    /**
     * 신규 사용자 생성
     */
    private User createNewUser(LoginType loginType, String providerId,
                               String email, String nickname) {
        User user = User.builder()
                .loginType(loginType)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .role(RoleType.ROLE_USER)
                .build();

        return userRepository.save(user);
    }
}