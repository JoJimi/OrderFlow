package org.example.user.security.service;

import lombok.RequiredArgsConstructor;
import org.example.shared.security.service.UserDetailsLoader;
import org.example.user.repository.UserRepository;
import org.example.user.security.userdetails.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsLoaderImpl implements UserDetailsLoader {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUserId(String userId) {
        try {
            Long id = Long.parseLong(userId);
            return new CustomUserDetails(
                    userRepository.findById(id)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId))
            );
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user id format: " + userId, e);
        }
    }
}