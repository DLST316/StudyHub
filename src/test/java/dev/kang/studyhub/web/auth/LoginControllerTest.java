package dev.kang.studyhub.web.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoginController 테스트 클래스
 * 
 * 테스트 목적:
 * - 로그인 페이지 접근 테스트
 * - 에러 메시지 표시 테스트
 * - 성공 메시지 표시 테스트
 */
@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("로그인 페이지 접근 테스트 - 정상 접근")
    void loginPage_정상접근() throws Exception {
        // given & when & then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("errorMessage"))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 테스트 - 에러 파라미터와 함께")
    void loginPage_에러파라미터와함께() throws Exception {
        // given & when & then
        mockMvc.perform(get("/login")
                        .param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 테스트 - 로그아웃 파라미터와 함께")
    void loginPage_로그아웃파라미터와함께() throws Exception {
        // given & when & then
        mockMvc.perform(get("/login")
                        .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("successMessage", "로그아웃되었습니다."))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    @DisplayName("로그인 페이지 접근 테스트 - 에러와 로그아웃 파라미터 모두 존재")
    void loginPage_에러와로그아웃파라미터모두존재() throws Exception {
        // given & when & then
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다."))
                .andExpect(model().attribute("successMessage", "로그아웃되었습니다."));
    }
} 