package org.example.user.repository;

import org.example.shared.type.common.LoginType;
import org.example.shared.type.common.RoleType;
import org.example.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    long countByRole(RoleType role);

    User save(User user);

    void delete(User user);
}