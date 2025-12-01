package org.example.user.repository.adapter;

import lombok.RequiredArgsConstructor;

import org.example.shared.type.LoginType;
import org.example.user.domain.User;
import org.example.user.repository.SpringDataUserRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId) {
        return springDataUserRepository.findByLoginTypeAndProviderId(loginType, providerId);
    }

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.delete(user);
    }
}
