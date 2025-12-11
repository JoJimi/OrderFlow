package org.example.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shared.security.annotation.CurrentUser;
import org.example.shared.security.userdetails.SecurityUser;
import org.example.user.dto.request.UserRoleUpdateRequest;
import org.example.user.dto.response.UserResponse;
import org.example.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getMyProfile(
            @CurrentUser SecurityUser securityUser) {

        // JWT의 userId는 String이므로 Long으로 변환
        Long userId = Long.parseLong(securityUser.getUserId());
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    public ResponseEntity<Void> deleteAccount(
            @CurrentUser SecurityUser securityUser) {

        // JWT의 userId는 String이므로 Long으로 변환
        Long userId = Long.parseLong(securityUser.getUserId());
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 사용자 목록 조회 (관리자 전용)",
            description = "모든 사용자 목록을 페이지네이션하여 조회합니다.")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 역할 변경 (관리자 전용)
     */
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 역할 변경 (관리자 전용)",
            description = "특정 사용자의 역할을 변경합니다. 자신의 역할은 변경할 수 없으며, 마지막 관리자는 강등할 수 없습니다.")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request,
            @CurrentUser SecurityUser securityUser) {

        // JWT의 userId는 String이므로 Long으로 변환
        Long currentUserId = Long.parseLong(securityUser.getUserId());
        UserResponse updated = userService.updateUserRole(userId, currentUserId, request.role());

        return ResponseEntity.ok(updated);
    }
}