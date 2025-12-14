package org.example.user.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.type.common.LoginType;
import org.example.user.domain.User;
import org.example.user.service.OAuth2UserRegistration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 카카오 OAuth2 사용자 정보 처리 서비스
 * - OAuth2 제공자로부터 사용자 정보를 로드
 * - DB에 사용자 등록 또는 업데이트
 * - CustomOAuth2User 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String KAKAO_ACCOUNT_KEY = "kakao_account";
    private static final String KAKAO_PROFILE_KEY = "profile";
    private static final String EMAIL_KEY = "email";
    private static final String NICKNAME_KEY = "nickname";

    private final DefaultOAuth2UserService delegate;
    private final OAuth2UserRegistration registration;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 제공자로부터 사용자 정보 로드
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // 2. 제공자 타입 확인 (카카오)
        LoginType loginType = determineLoginType(userRequest);

        // 3. Provider ID 추출
        String providerId = extractProviderId(userRequest, oauth2User);

        // 4. 카카오 계정 정보 추출
        KakaoUserInfo kakaoInfo = extractKakaoUserInfo(oauth2User.getAttributes());

        // 5. DB에 사용자 등록 또는 업데이트
        User user = registration.registerOrUpdate(
                loginType,
                providerId,
                kakaoInfo.email(),
                kakaoInfo.nickname()
        );

        // 6. 권한 부여
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        // 7. CustomOAuth2User 반환
        return new CustomOAuth2User(user, oauth2User.getAttributes(), authorities);
    }

    /**
     * 로그인 타입 결정
     */
    private LoginType determineLoginType(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        return LoginType.fromProvider(registrationId)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error(
                                "invalid_provider",
                                "지원하지 않는 로그인 제공자: " + registrationId,
                                null
                        )
                ));
    }

    /**
     * Provider ID 추출
     */
    private String extractProviderId(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Object providerIdObj = oauth2User.getAttributes().get(userNameAttribute);

        if (providerIdObj == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "invalid_provider_id",
                            "제공자 ID가 응답에 없습니다",
                            null
                    )
            );
        }

        return providerIdObj.toString();
    }

    /**
     * 카카오 사용자 정보 추출
     */
    private KakaoUserInfo extractKakaoUserInfo(Map<String, Object> attributes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(KAKAO_ACCOUNT_KEY);

        if (kakaoAccount == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "no_kakao_account",
                            "카카오 계정 정보가 없습니다",
                            null
                    )
            );
        }
        String email = extractEmail(kakaoAccount);  // 이메일 추출
        String nickname = extractNickname(kakaoAccount);    // 닉네임 추출

        return new KakaoUserInfo(email, nickname);
    }

    /**
     * 이메일 추출
     */
    private String extractEmail(Map<String, Object> kakaoAccount) {
        Object emailObj = kakaoAccount.get(EMAIL_KEY);

        if (emailObj == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "no_email",
                            "카카오 계정에 이메일 동의가 필요합니다",
                            null
                    )
            );
        }

        return emailObj.toString();
    }

    /**
     * 닉네임 추출
     */
    private String extractNickname(Map<String, Object> kakaoAccount) {
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get(KAKAO_PROFILE_KEY);

        if (profile == null) {
            return "Unknown";
        }

        Object nicknameObj = profile.get(NICKNAME_KEY);
        return nicknameObj != null ? nicknameObj.toString() : "Unknown";
    }

    /**
     * 카카오 사용자 정보 레코드
     */
    private record KakaoUserInfo(String email, String nickname) {}
}