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

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        user1 = User.builder()
                .name("테스트 사용자1")
                .email("test1@example.com")
                .password("password123")
                .role("USER")
                .isBlocked(false)
                .educationStatus(EducationStatus.ENROLLED)
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .name("테스트 사용자2")
                .email("test2@example.com")
                .password("password456")
                .role("USER")
                .isBlocked(false)
                .educationStatus(EducationStatus.GRADUATED)
                .university("테스트 대학교")
                .major("소프트웨어공학과")
                .build();
        userRepository.save(user2);

        blockedUser = User.builder()
                .name("차단된 사용자")
                .email("blocked@example.com")
                .password("password789")
                .role("USER")
                .isBlocked(true)
                .educationStatus(EducationStatus.ENROLLED)
                .university("테스트 대학교")
                .major("정보통신공학과")
                .build();
        userRepository.save(blockedUser);

        adminUser = User.builder()
                .name("관리자")
                .email("admin@example.com")
                .password("admin123")
                .role("ADMIN")
                .isBlocked(false)
                .educationStatus(EducationStatus.GRADUATED)
                .university("관리자 대학교")
                .major("관리학과")
                .build();
        userRepository.save(adminUser);
    }

    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail() {
        // when
        Optional<User> found = userRepository.findByEmail("test1@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 사용자1");
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
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
        boolean exists1 = userRepository.existsByEmail("test1@example.com");
        boolean exists2 = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists1).isTrue();
        assertThat(exists2).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자 삭제")
    void deleteByEmail() {
        // given
        assertThat(userRepository.findByEmail("test1@example.com")).isPresent();

        // when
        userRepository.deleteByEmail("test1@example.com");

        // then
        assertThat(userRepository.findByEmail("test1@example.com")).isEmpty();
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
        User newUser = User.builder()
                .name("새로운 사용자")
                .email("new@example.com")
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
        assertThat(found.getEmail()).isEqualTo("new@example.com");
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