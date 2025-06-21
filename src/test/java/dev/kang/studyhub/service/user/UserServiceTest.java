package dev.kang.studyhub.service.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException;
import dev.kang.studyhub.web.user.UserJoinForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 클래스의 단위 테스트
 * 
 * 테스트 대상:
 * - 회원가입 기능 (join)
 * - 사용자 조회 기능 (findByEmail)
 * - 사용자 저장 기능 (save)
 * 
 * 테스트 시나리오:
 * - 정상적인 회원가입
 * - 중복 이메일로 인한 회원가입 실패
 * - 비밀번호 암호화 검증
 * - 사용자 조회 성공/실패
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserJoinForm validJoinForm;
    private User savedUser;

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 유효한 회원가입 폼 데이터 생성
        validJoinForm = new UserJoinForm();
        validJoinForm.setName("테스트 사용자");
        validJoinForm.setEmail("test@example.com");
        validJoinForm.setPassword("password123");
        validJoinForm.setUniversity("테스트 대학교");
        validJoinForm.setMajor("컴퓨터공학과");
        validJoinForm.setEducationStatus(EducationStatus.ENROLLED);

        // 저장될 사용자 엔티티 생성
        savedUser = User.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@example.com")
                .password("encodedPassword123")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입이 성공해야 한다")
    void join_Success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.join(validJoinForm);

        // then
        verify(userRepository, times(1)).findByEmail(validJoinForm.getEmail());
        verify(passwordEncoder, times(1)).encode(validJoinForm.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생해야 한다")
    void join_DuplicateEmail_ThrowsException() {
        // given
        when(userRepository.findByEmail(validJoinForm.getEmail())).thenReturn(Optional.of(savedUser));

        // when & then
        assertThatThrownBy(() -> userService.join(validJoinForm))
                .isInstanceOf(AlreadyExistsEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        verify(userRepository, times(1)).findByEmail(validJoinForm.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호가 올바르게 암호화되어야 한다")
    void join_PasswordShouldBeEncoded() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validJoinForm.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.join(validJoinForm);

        // then
        verify(passwordEncoder, times(1)).encode(validJoinForm.getPassword());
        verify(userRepository).save(argThat(user -> 
            user.getPassword().equals("encodedPassword123") &&
            user.getRole().equals("USER")
        ));
    }

    @Test
    @DisplayName("이메일로 사용자를 찾을 수 있어야 한다")
    void findByEmail_Success() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));

        // when
        Optional<User> result = userService.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환해야 한다")
    void findByEmail_NotFound_ReturnsEmptyOptional() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.findByEmail(email);

        // then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 정보를 저장할 수 있어야 한다")
    void save_Success() {
        // given
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.save(savedUser);

        // then
        verify(userRepository, times(1)).save(savedUser);
    }

    @Test
    @DisplayName("회원가입 시 사용자 엔티티가 올바른 정보로 생성되어야 한다")
    void join_CreatesUserWithCorrectInformation() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        userService.join(validJoinForm);

        // then
        verify(userRepository).save(argThat(user -> 
            user.getName().equals(validJoinForm.getName()) &&
            user.getEmail().equals(validJoinForm.getEmail()) &&
            user.getUniversity().equals(validJoinForm.getUniversity()) &&
            user.getMajor().equals(validJoinForm.getMajor()) &&
            user.getEducationStatus().equals(validJoinForm.getEducationStatus()) &&
            user.getRole().equals("USER")
        ));
    }
} 