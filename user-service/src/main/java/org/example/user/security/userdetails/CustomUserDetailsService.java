package org.example.user.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.example.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// OAuth2 로그인 프로필 가져와 DB에 회원 정보 저장 및 조회 후 OAuth2User 구현체 반환
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(id);
            return new CustomUserDetails(
                    userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(id + " not found")));
        } catch (NumberFormatException ex) {
            throw new UsernameNotFoundException("Invalid user id: " + id, ex);
        }
    }
}
