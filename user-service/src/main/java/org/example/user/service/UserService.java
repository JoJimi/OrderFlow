package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.exception.ErrorCode;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.user.UserNotFoundException;
import org.example.shared.type.common.RoleType;
import org.example.user.domain.User;
import org.example.user.dto.response.UserResponse;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 조회 (내부용 - User 엔티티 반환)
     */
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * 사용자 ID로 조회 (API용 - UserResponse 반환)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("사용자 조회: userId={}", userId);
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    /**
     * 사용자 삭제 (탈퇴)
     * - 마지막 관리자는 탈퇴할 수 없음
     */
    public void deleteUser(Long userId) {
        User user = findUserById(userId);

        // 마지막 관리자 탈퇴 방지
        if (user.getRole() == RoleType.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(RoleType.ROLE_ADMIN);
            if (adminCount <= 1) {
                log.warn("마지막 관리자 탈퇴 시도: userId={}, email={}",
                        userId, user.getEmail());
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "마지막 관리자는 탈퇴할 수 없습니다. 다른 관리자를 지정한 후 탈퇴해주세요."
                );
            }
        }

        user.markAsDeleted();  // 논리 삭제
        userRepository.save(user);

        log.info("사용자 탈퇴 완료: userId={}, email={}, role={}",
                userId, user.getEmail(), user.getRole());
    }

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("전체 사용자 목록 조회: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    /**
     * 사용자 역할 변경 (관리자 전용)
     */
    public UserResponse updateUserRole(Long userId, Long currentUserId, RoleType newRole) {
        // 1. 대상 사용자 조회
        User targetUser = findUserById(userId);

        // 2. 자기 자신의 역할 변경 금지
        if (userId.equals(currentUserId)) {
            log.warn("자기 자신의 역할 변경 시도: userId={}", userId);
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "자신의 역할은 변경할 수 없습니다."
            );
        }

        // 3. ADMIN → USER 강등 시 마지막 관리자인지 확인
        if (targetUser.getRole() == RoleType.ROLE_ADMIN && newRole == RoleType.ROLE_USER) {
            long adminCount = userRepository.countByRole(RoleType.ROLE_ADMIN);
            if (adminCount <= 1) {
                log.warn("마지막 관리자 강등 시도: userId={}, adminCount={}",
                        userId, adminCount);
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "마지막 관리자는 강등할 수 없습니다."
                );
            }
        }

        // 4. 역할 변경
        RoleType oldRole = targetUser.getRole();
        targetUser.updateRole(newRole);
        User updatedUser = userRepository.save(targetUser);

        log.info("사용자 역할 변경 완료: userId={}, oldRole={}, newRole={}, changedBy={}",
                userId, oldRole, newRole, currentUserId);

        return UserResponse.from(updatedUser);
    }
}