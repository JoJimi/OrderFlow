package org.example.user.repository;

import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;
import org.example.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA Repository
 */
public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    /**
     * 소셜 로그인에서 내려준 providerId와 loginType(Google/Kakao)으로 조회
     */
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 전체 사용자 조회 (페이지네이션)
     */
    Page<User> findAll(Pageable pageable);

    /**
     * 특정 역할의 사용자 수 조회
     */
    long countByRole(RoleType role);
}