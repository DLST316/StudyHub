package dev.kang.studyhub.service.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * CustomUserDetailsService 클래스의 단위 테스트
 * 
 * 테스트 대상:
 * - Spring Security와의 연동 기능
 * - 사용자 정보를 UserDetails로 변환하는 기능
 * - 존재하지 않는 사용자에 대한 예외 처리
 * 
 * 테스트 시나리오:
 * - 정상적인 사용자 로드
 * - 존재하지 않는 사용자 조회 시 예외 발생
 * - UserDetails 객체의 올바른 생성
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 엔티티 생성
        testUser = User.builder()
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
    @DisplayName("이메일로 사용자를 찾아 UserDetails를 반환해야 한다")
    void loadUserByUsername_Success() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 UsernameNotFoundException이 발생해야 한다")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: " + email);
    }

    @Test
    @DisplayName("ADMIN 역할을 가진 사용자의 권한이 올바르게 설정되어야 한다")
    void loadUserByUsername_AdminUser_HasAdminRole() {
        // given
        User adminUser = User.builder()
                .id(2L)
                .name("관리자")
                .email("admin@example.com")
                .password("adminPassword123")
                .role("ADMIN")
                .build();
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("UserDetails 객체가 Spring Security에서 사용할 수 있는 형태로 생성되어야 한다")
    void loadUserByUsername_CreatesValidUserDetails() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("빈 문자열로 조회 시에도 예외가 발생해야 한다")
    void loadUserByUsername_EmptyEmail_ThrowsException() {
        // given
        String email = "";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: " + email);
    }

    @Test
    @DisplayName("null로 조회 시에도 예외가 발생해야 한다")
    void loadUserByUsername_NullEmail_ThrowsException() {
        // given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: null");
    }
} 