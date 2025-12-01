package org.example.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.user.domain.User;
import org.example.user.dto.response.UserResponse;
import org.example.user.security.userdetails.CustomUserDetails;
import org.example.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 정보 조회 기능
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal){

        return ResponseEntity.ok(UserResponse.from(principal.getUser()));
    }

    // 회원탈퇴 기능
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails principal) {

        userService.deleteUser(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

}
