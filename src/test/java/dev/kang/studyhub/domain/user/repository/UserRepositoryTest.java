package dev.kang.studyhub.domain.user.repository;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 클래스의 데이터 접근 계층 테스트
 * 
 * 테스트 대상:
 * - 사용자 저장 (save)
 * - 이메일로 사용자 조회 (findByEmail)
 * - JPA 엔티티 매핑 검증
 * 
 * 테스트 시나리오:
 * - 정상적인 사용자 저장 및 조회
 * - 존재하지 않는 이메일로 조회
 * - 중복 이메일 처리
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장하고 이메일로 조회할 수 있어야 한다")
    void saveAndFindByEmail_Success() {
        // given
        User user = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .password("encodedPassword123")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getName()).isEqualTo("테스트 사용자");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환해야 한다")
    void findByEmail_NotFound_ReturnsEmptyOptional() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자를 저장하고 각각 조회할 수 있어야 한다")
    void saveMultipleUsersAndFind_Success() {
        // given
        User user1 = User.builder()
                .name("사용자1")
                .email("user1@example.com")
                .password("password1")
                .role("USER")
                .build();

        User user2 = User.builder()
                .name("사용자2")
                .email("user2@example.com")
                .password("password2")
                .role("USER")
                .build();

        // when
        userRepository.save(user1);
        userRepository.save(user2);

        Optional<User> foundUser1 = userRepository.findByEmail("user1@example.com");
        Optional<User> foundUser2 = userRepository.findByEmail("user2@example.com");

        // then
        assertThat(foundUser1).isPresent();
        assertThat(foundUser1.get().getName()).isEqualTo("사용자1");
        assertThat(foundUser2).isPresent();
        assertThat(foundUser2.get().getName()).isEqualTo("사용자2");
    }

    @Test
    @DisplayName("사용자 엔티티의 모든 필드가 올바르게 저장되어야 한다")
    void saveUser_AllFieldsAreSaved() {
        // given
        User user = User.builder()
                .name("전체 필드 테스트")
                .email("full@example.com")
                .password("encodedPassword")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.GRADUATED)
                .role("ADMIN")
                .build();

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("full@example.com");

        // then
        assertThat(foundUser).isPresent();
        User found = foundUser.get();
        assertThat(found.getName()).isEqualTo("전체 필드 테스트");
        assertThat(found.getEmail()).isEqualTo("full@example.com");
        assertThat(found.getPassword()).isEqualTo("encodedPassword");
        assertThat(found.getUniversity()).isEqualTo("테스트 대학교");
        assertThat(found.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(found.getEducationStatus()).isEqualTo(EducationStatus.GRADUATED);
        assertThat(found.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("사용자 정보를 업데이트할 수 있어야 한다")
    void updateUser_Success() {
        // given
        User user = User.builder()
                .name("원본 이름")
                .email("update@example.com")
                .password("password")
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        // when
        savedUser.setName("업데이트된 이름");
        savedUser.setMajor("업데이트된 전공");
        User updatedUser = userRepository.save(savedUser);

        Optional<User> foundUser = userRepository.findByEmail("update@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("업데이트된 이름");
        assertThat(foundUser.get().getMajor()).isEqualTo("업데이트된 전공");
    }

    @Test
    @DisplayName("빈 문자열로 조회 시 빈 Optional을 반환해야 한다")
    void findByEmail_EmptyString_ReturnsEmptyOptional() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("");

        // then
        assertThat(foundUser).isEmpty();
    }
} 