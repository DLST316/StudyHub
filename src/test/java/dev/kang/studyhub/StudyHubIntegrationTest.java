package dev.kang.studyhub;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyHub 애플리케이션의 통합 테스트
 * 
 * 테스트 대상:
 * - 전체 애플리케이션 컨텍스트 로딩
 * - 웹 계층과 서비스 계층의 연동
 * - 데이터베이스 연동
 * - Spring Security 설정
 * 
 * 테스트 시나리오:
 * - 애플리케이션 시작 및 컨텍스트 로딩
 * - 회원가입부터 로그인까지의 전체 플로우
 * - 웹 페이지 접근 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class StudyHubIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로딩되어야 한다")
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로딩되는지 확인
        assertThat(userService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("홈페이지에 정상적으로 접근할 수 있어야 한다")
    void homePage_Accessible() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("StudyHub");
    }

    @Test
    @DisplayName("회원가입 페이지에 정상적으로 접근할 수 있어야 한다")
    void joinPage_Accessible() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/join", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("회원가입");
    }

    @Test
    @DisplayName("회원가입부터 데이터베이스 저장까지 전체 플로우가 정상적으로 동작해야 한다")
    void joinFlow_CompleteSuccess() {
        // given
        UserJoinForm joinForm = new UserJoinForm();
        joinForm.setName("통합 테스트 사용자");
        joinForm.setEmail("integration@example.com");
        joinForm.setPassword("password123");
        joinForm.setUniversity("통합 테스트 대학교");
        joinForm.setMajor("통합 테스트 전공");
        joinForm.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm);

        // then
        Optional<User> savedUser = userRepository.findByEmail("integration@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("통합 테스트 사용자");
        assertThat(savedUser.get().getUniversity()).isEqualTo("통합 테스트 대학교");
        assertThat(savedUser.get().getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외가 발생해야 한다")
    void joinFlow_DuplicateEmail_ThrowsException() {
        // given
        UserJoinForm joinForm1 = new UserJoinForm();
        joinForm1.setName("첫 번째 사용자");
        joinForm1.setEmail("duplicate@example.com");
        joinForm1.setPassword("password123");
        joinForm1.setEducationStatus(EducationStatus.ENROLLED);

        UserJoinForm joinForm2 = new UserJoinForm();
        joinForm2.setName("두 번째 사용자");
        joinForm2.setEmail("duplicate@example.com");
        joinForm2.setPassword("password456");
        joinForm2.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm1);

        // then
        assertThat(userRepository.findByEmail("duplicate@example.com")).isPresent();
        
        // 중복 이메일로 가입 시도
        try {
            userService.join(joinForm2);
            assertThat(false).isTrue(); // 예외가 발생해야 함
        } catch (dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException e) {
            assertThat(e.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
        }
    }

    @Test
    @DisplayName("비밀번호가 암호화되어 저장되어야 한다")
    void joinFlow_PasswordEncoded() {
        // given
        UserJoinForm joinForm = new UserJoinForm();
        joinForm.setName("암호화 테스트");
        joinForm.setEmail("encode@example.com");
        joinForm.setPassword("plainPassword");
        joinForm.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm);

        // then
        Optional<User> savedUser = userRepository.findByEmail("encode@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).isNotEqualTo("plainPassword");
        assertThat(savedUser.get().getPassword()).startsWith("$2a$"); // BCrypt 해시 형식
    }
} 