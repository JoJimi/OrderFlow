package org.example.shared.security.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT에서 추출한 userId로 실제 UserDetails 로드
 * - user-service: CustomUserDetails 반환 (DB 조회)
 * - 다른 서비스: 구현 안 함 또는 간단한 구현
 */
public interface UserDetailsLoader {
    UserDetails loadUserByUserId(String userId);
}