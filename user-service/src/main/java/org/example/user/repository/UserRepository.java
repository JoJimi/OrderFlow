package org.example.user.repository;


import org.example.shared.type.LoginType;
import org.example.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);
    User save(User user);
    void delete(User user);

}