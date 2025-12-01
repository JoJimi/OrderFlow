package org.example.user.service;

import lombok.RequiredArgsConstructor;
import org.example.shared.exception.user.UserNotFoundException;
import org.example.user.domain.User;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 현재 로그인된 사용자의 정보를 조회합니다.
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    // 현재 로그인된 사용자를 삭제(탈퇴)합니다.
    public void deleteUser(Long userId) {
        userRepository.delete(findUserById(userId));
    }
}
