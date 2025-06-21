package dev.kang.studyhub.web;

import dev.kang.studyhub.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController 클래스의 웹 계층 테스트
 * 
 * 테스트 대상:
 * - 홈페이지 접근 (GET /)
 * - 로그인 상태에 따른 조건부 렌더링
 * - 성공 메시지 파라미터 처리
 * 
 * 테스트 시나리오:
 * - 로그인하지 않은 사용자의 홈페이지 접근
 * - 로그인한 사용자의 홈페이지 접근
 * - 성공 메시지와 함께 홈페이지 접근
 */
@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않은 사용자가 홈페이지에 접근할 수 있어야 한다")
    void home_NotLoggedIn_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false));
    }

    @Test
    @DisplayName("로그인한 사용자가 홈페이지에 접근할 수 있어야 한다")
    void home_LoggedIn_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false)); // 테스트 환경에서는 항상 false
    }

    @Test
    @DisplayName("회원가입 성공 메시지와 함께 홈페이지에 접근할 수 있어야 한다")
    void home_WithSuccessMessage_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."));
    }

    @Test
    @DisplayName("알 수 없는 성공 메시지 파라미터는 무시되어야 한다")
    void home_WithUnknownSuccessMessage_IgnoresMessage() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }

    @Test
    @DisplayName("로그인한 사용자가 성공 메시지와 함께 홈페이지에 접근할 수 있어야 한다")
    void home_LoggedInWithSuccessMessage_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."));
    }

    @Test
    @DisplayName("홈페이지는 성공 메시지 파라미터 없이도 정상적으로 접근할 수 있어야 한다")
    void home_WithoutSuccessMessage_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }
} 