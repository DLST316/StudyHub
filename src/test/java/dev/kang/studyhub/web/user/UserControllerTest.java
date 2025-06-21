package dev.kang.studyhub.web.user;

import dev.kang.studyhub.config.TestSecurityConfig;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 클래스의 웹 계층 테스트
 * 
 * 테스트 대상:
 * - 회원가입 폼 표시 (GET /join)
 * - 회원가입 처리 (POST /join)
 * - 로그아웃 처리 (GET /logout)
 * 
 * 테스트 시나리오:
 * - 정상적인 회원가입 폼 접근
 * - 유효한 회원가입 데이터 처리
 * - 유효하지 않은 회원가입 데이터 처리
 * - 중복 이메일로 인한 회원가입 실패
 * - 로그아웃 처리
 */
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("회원가입 폼 페이지가 정상적으로 표시되어야 한다")
    void showJoinForm_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/join"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().attributeExists("userJoinForm"));
    }

    @Test
    @DisplayName("유효한 회원가입 데이터로 가입이 성공해야 한다")
    void processJoin_ValidData_Success() throws Exception {
        // given
        doNothing().when(userService).join(any(UserJoinForm.class));

        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("university", "테스트 대학교")
                        .param("major", "컴퓨터공학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?success=join"));
    }

    @Test
    @DisplayName("이름이 비어있으면 회원가입 폼으로 다시 이동해야 한다")
    void processJoin_EmptyName_ReturnsToForm() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 회원가입 폼으로 다시 이동해야 한다")
    void processJoin_InvalidEmail_ReturnsToForm() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "invalid-email")
                        .param("password", "password123")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("비밀번호가 6자 미만이면 회원가입 폼으로 다시 이동해야 한다")
    void processJoin_ShortPassword_ReturnsToForm() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "test@example.com")
                        .param("password", "123")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("학력 상태가 선택되지 않으면 회원가입 폼으로 다시 이동해야 한다")
    void processJoin_NoEducationStatus_ReturnsToForm() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("중복된 이메일로 가입 시 회원가입 폼으로 다시 이동해야 한다")
    void processJoin_DuplicateEmail_ReturnsToForm() throws Exception {
        // given
        doThrow(new dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException("이미 사용 중인 이메일입니다."))
                .when(userService).join(any(UserJoinForm.class));

        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"));
    }

    @Test
    @DisplayName("로그아웃이 정상적으로 처리되어야 한다")
    void logout_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("회원가입 시 선택적 필드들이 정상적으로 처리되어야 한다")
    void processJoin_OptionalFields_Success() throws Exception {
        // given
        doNothing().when(userService).join(any(UserJoinForm.class));

        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("university", "")  // 선택적 필드
                        .param("major", "")       // 선택적 필드
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?success=join"));
    }
} 