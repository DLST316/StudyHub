package dev.kang.studyhub.web.user;

import dev.kang.studyhub.user.dto.UserJoinForm;
import dev.kang.studyhub.user.service.UserService;
import dev.kang.studyhub.user.model.EducationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(dev.kang.studyhub.config.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("회원가입 폼 페이지 접근")
    void showJoinForm() throws Exception {
        // when & then
        mockMvc.perform(get("/join"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"))
                .andExpect(model().attributeExists("userJoinForm"));
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void processJoin_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "password123")
                        .param("university", "테스트 대학교")
                        .param("major", "컴퓨터공학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?success=join"));
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 실패 (이름 누락)")
    void processJoin_ValidationError_Name() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "")  // 이름이 비어있음
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "password123")
                        .param("university", "테스트 대학교")
                        .param("major", "컴퓨터공학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"));
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 실패 (아이디 누락)")
    void processJoin_ValidationError_Username() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("username", "")  // 아이디가 비어있음
                        .param("email", "test@test.com")
                        .param("password", "password123")
                        .param("university", "테스트 대학교")
                        .param("major", "컴퓨터공학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"));
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 실패 (비밀번호 누락)")
    void processJoin_ValidationError_Password() throws Exception {
        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "테스트 사용자")
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "")  // 비밀번호가 비어있음
                        .param("university", "테스트 대학교")
                        .param("major", "컴퓨터공학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"));
    }

    @Test
    @DisplayName("회원가입 - 중복 이메일")
    void processJoin_DuplicateEmail() throws Exception {
        // given
        createTestUser();

        // when & then
        mockMvc.perform(post("/join")
                        .param("name", "다른 사용자")
                        .param("username", "differentuser")
                        .param("email", "test@test.com")  // 이미 존재하는 이메일
                        .param("password", "password123")
                        .param("university", "다른 대학교")
                        .param("major", "다른 학과")
                        .param("educationStatus", "ENROLLED"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/join"));
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        // given - 먼저 로그인
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection());

        // when & then - 로그아웃
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    /**
     * 테스트용 사용자 생성
     */
    private void createTestUser() {
        UserJoinForm form = new UserJoinForm();
        form.setName("테스트 사용자");
        form.setUsername("testuser");
        form.setEmail("test@test.com");
        form.setPassword("password123");
        form.setUniversity("테스트 대학교");
        form.setMajor("컴퓨터공학과");
        form.setEducationStatus(EducationStatus.ENROLLED);
        
        userService.join(form);
    }
} 