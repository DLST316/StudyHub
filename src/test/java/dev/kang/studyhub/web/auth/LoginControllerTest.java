package dev.kang.studyhub.web.auth;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoginController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoginControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 기본")
    void loginPage_Default() throws Exception {
        // when & then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 에러 파라미터와 함께")
    void loginPage_WithError() throws Exception {
        // when & then
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 로그아웃 파라미터와 함께")
    void loginPage_WithLogout() throws Exception {
        // when & then
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("successMessage", "로그아웃되었습니다."));
    }

    @Test
    @DisplayName("로그인 페이지 접근 - 에러와 로그아웃 파라미터 모두")
    void loginPage_WithErrorAndLogout() throws Exception {
        // when & then
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(model().attribute("successMessage", "로그아웃되었습니다."));
    }
} 