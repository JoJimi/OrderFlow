package org.example.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.shared.type.RoleType;

@Schema(description = "사용자 역할 변경 요청")
public record UserRoleUpdateRequest(
        @NotNull(message = "역할은 필수입니다.")
        @Schema(description = "변경할 역할", example = "ROLE_ADMIN",
                allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
        RoleType role
) {}