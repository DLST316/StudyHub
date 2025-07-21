package dev.kang.studyhub.web;

import dev.kang.studyhub.study.service.StudyService;
import dev.kang.studyhub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HomeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyService studyService;

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
    @DisplayName("홈페이지 접근 - 로그인하지 않은 사용자")
    void homePage_NotLoggedIn() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("홈페이지 접근 - 로그인한 사용자")
    void homePage_LoggedIn() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    @DisplayName("홈페이지 접근 - 회원가입 성공 메시지와 함께")
    void homePage_WithJoinSuccessMessage() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."));
    }

    @Test
    @DisplayName("홈페이지 접근 - 로그인 성공 메시지와 함께")
    void homePage_WithLoginSuccessMessage() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "login"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "로그인되었습니다."));
    }

    @Test
    @DisplayName("홈페이지에서 최근 스터디 목록 조회")
    void homePage_RecentStudiesList() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"));
    }
} 