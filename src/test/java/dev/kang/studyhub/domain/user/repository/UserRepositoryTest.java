package dev.kang.studyhub.domain.user.repository;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 통합 테스트
 *
 * - 사용자 CRUD 작업
 * - 이메일 기반 조회 및 검증
 * - 차단된 사용자 관리
 * - 페이징 처리
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User blockedUser;
    private User adminUser;

    private String user1Username;
    private String user1Email;
    private String user2Username;
    private String user2Email;
    private String blockedUsername;
    private String blockedEmail;
    private String adminUsername;
    private String adminEmail;

    @BeforeEach
    void setUp() {
        // username/email 길이 제한(50자) 내에서 고유값 생성
        user1Username = "testuser1_" + UUID.randomUUID().toString().substring(0, 8);
        user1Email = "test1_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        user2Username = "testuser2_" + UUID.randomUUID().toString().substring(0, 8);
        user2Email = "test2_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        blockedUsername = "blocked_" + UUID.randomUUID().toString().substring(0, 8);
        blockedEmail = "blocked_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        adminUsername = "admin_" + UUID.randomUUID().toString().substring(0, 8);
        adminEmail = "admin_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        user1 = createTestUser("테스트 사용자1", user1Username, user1Email, false, EducationStatus.ENROLLED, "테스트 대학교", "컴퓨터공학과", "USER");
        user2 = createTestUser("테스트 사용자2", user2Username, user2Email, false, EducationStatus.GRADUATED, "테스트 대학교", "소프트웨어공학과", "USER");
        blockedUser = createTestUser("차단된 사용자", blockedUsername, blockedEmail, true, EducationStatus.ENROLLED, "테스트 대학교", "정보통신공학과", "USER");
        adminUser = createTestUser("관리자", adminUsername, adminEmail, false, EducationStatus.GRADUATED, "관리자 대학교", "관리학과", "ADMIN");
    }

    /**
     * 테스트용 사용자 생성 유틸 (username/email/차단여부/role 등 파라미터화)
     */
    private User createTestUser(String name, String username, String email, boolean isBlocked, EducationStatus educationStatus, String university, String major, String role) {
        User user = User.builder()
                .name(name)
                .username(username)
                .email(email)
                .password("password123")
                .role(role)
                .isBlocked(isBlocked)
                .educationStatus(educationStatus)
                .university(university)
                .major(major)
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail() {
        // when
        Optional<User> found = userRepository.findByEmail(user1Email);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 사용자1");
        assertThat(found.get().getEmail()).isEqualTo(user1Email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회")
    void findByEmail_NotFound() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        // when
        boolean exists1 = userRepository.existsByEmail(user1Email);
        boolean exists2 = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists1).isTrue();
        assertThat(exists2).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자 삭제")
    void deleteByEmail() {
        // given
        assertThat(userRepository.findByEmail(user1Email)).isPresent();

        // when
        userRepository.deleteByEmail(user1Email);

        // then
        assertThat(userRepository.findByEmail(user1Email)).isEmpty();
    }

    @Test
    @DisplayName("차단된 유저 수 조회")
    void countByIsBlockedTrue() {
        // when
        long blockedCount = userRepository.countByIsBlockedTrue();

        // then
        assertThat(blockedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFind() {
        // given
        String newUsername = "newuser_" + UUID.randomUUID().toString().substring(0, 8);
        String newEmail = "new_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        User newUser = User.builder()
                .name("새로운 사용자")
                .username(newUsername)
                .email(newEmail)
                .password("newpassword")
                .role("USER")
                .isBlocked(false)
                .educationStatus(EducationStatus.ENROLLED)
                .university("새로운 대학교")
                .major("새로운 학과")
                .build();

        // when
        User saved = userRepository.save(newUser);
        User found = userRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("새로운 사용자");
        assertThat(found.getEmail()).isEqualTo(newEmail);
        assertThat(found.getUniversity()).isEqualTo("새로운 대학교");
        assertThat(found.getMajor()).isEqualTo("새로운 학과");
        assertThat(found.getEducationStatus()).isEqualTo(EducationStatus.ENROLLED);
    }

    @Test
    @DisplayName("사용자 정보 수정")
    void updateUser() {
        // given
        assertThat(user1.getName()).isEqualTo("테스트 사용자1");

        // when
        user1.setName("수정된 사용자");
        user1.setUniversity("수정된 대학교");
        user1.setMajor("수정된 학과");
        userRepository.save(user1);
        User updated = userRepository.findById(user1.getId()).orElse(null);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("수정된 사용자");
        assertThat(updated.getUniversity()).isEqualTo("수정된 대학교");
        assertThat(updated.getMajor()).isEqualTo("수정된 학과");
    }

    @Test
    @DisplayName("사용자 차단 상태 변경")
    void updateBlockStatus() {
        // given
        assertThat(user1.isBlocked()).isFalse();

        // when
        user1.setBlocked(true);
        userRepository.save(user1);
        User updated = userRepository.findById(user1.getId()).orElse(null);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.isBlocked()).isTrue();
        assertThat(userRepository.countByIsBlockedTrue()).isEqualTo(2);
    }
} 