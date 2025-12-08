package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserRegistration {

    private final UserRepository userRepository;

    @Value("${app.admin.emails:}")
    private List<String> adminEmails;

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
     * - 관리자 이메일 목록에 있으면 ROLE_ADMIN 부여
     */
    private User createNewUser(LoginType loginType, String providerId,
                               String email, String nickname) {

        // 관리자 이메일 확인
        RoleType role = isAdminEmail(email)
                ? RoleType.ROLE_ADMIN
                : RoleType.ROLE_USER;

        User user = User.builder()
                .loginType(loginType)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        log.info("신규 사용자 등록: userId={}, email={}, role={}",
                savedUser.getId(), email, role);

        return savedUser;
    }

    /**
     * 관리자 이메일 여부 확인
     */
    private boolean isAdminEmail(String email) {
        if (adminEmails == null || adminEmails.isEmpty()) {
            return false;
        }

        boolean isAdmin = adminEmails.stream()
                .anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email));

        if (isAdmin) {
            log.info("관리자 이메일 감지: {}", email);
        }

        return isAdmin;
    }
}