package dev.kang.studyhub.domain.user.entity;

import dev.kang.studyhub.domain.user.model.EducationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 아이디 (로그인용)
     * 필수 입력 항목이며, 고유해야 합니다.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 사용자 이메일 (연락처용)
     * 선택 입력 항목이며, 고유해야 합니다.
     */
    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_status", length = 20)
    private EducationStatus educationStatus;

    @Column(length = 100)
    private String university;

    @Column(length = 100)
    private String major;

    @Column(length = 20)
    private String role = "USER";

    /**
     * 차단 여부 (true: 차단됨, false: 정상)
     */
    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private boolean isBlocked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}