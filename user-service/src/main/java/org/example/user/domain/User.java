package org.example.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.LoginType;
import org.example.shared.type.RoleType;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_login_provider",
                        columnNames = {"login_type", "provider_id"})
        },
        indexes = {
                @Index(name = "idx_email", columnList = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String providerId;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20)
    private LoginType loginType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType role = RoleType.ROLE_USER;

    /**
     * 프로필 업데이트 (이메일, 닉네임)
     * @return 변경 여부
     */
    public boolean updateProfile(String newEmail, String newNickname) {
        boolean changed = false;

        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            changed = true;
        }

        if (newNickname != null && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
            changed = true;
        }

        return changed;
    }
}