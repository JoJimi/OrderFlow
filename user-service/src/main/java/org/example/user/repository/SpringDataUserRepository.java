package org.example.user.repository;

import org.example.shared.type.LoginType;
import org.example.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    /**
     * 소셜 로그인에서 내려준 providerId와 loginType(Google/Kakao)으로 조회
     */
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);

}
