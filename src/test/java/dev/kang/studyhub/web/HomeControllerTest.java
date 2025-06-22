package dev.kang.studyhub.web;

import dev.kang.studyhub.config.TestSecurityConfig;
import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController 클래스의 웹 계층 테스트
 * 
 * 테스트 대상:
 * - 홈페이지 접근 (GET /)
 * - 로그인하지 않은 사용자의 홈페이지 접근
 * - 로그인한 사용자의 홈페이지 접근
 * - 회원가입 성공 메시지와 함께 홈페이지 접근
 * 
 * 테스트 시나리오:
 * - 정상적인 홈페이지 접근
 * - 인증 상태에 따른 다른 화면 표시
 * - 성공 메시지 표시
 */
@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyService studyService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Study testStudy;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .password("encodedPassword")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        testStudy = Study.builder()
                .title("테스트 스터디")
                .description("테스트 스터디 설명")
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();

        // StudyService Mock 설정
        Pageable pageable = PageRequest.of(0, 6);
        Page<Study> studyPage = new PageImpl<>(List.of(testStudy), pageable, 1);
        when(studyService.findAll(any(Pageable.class))).thenReturn(studyPage);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 홈페이지에 접근할 수 있어야 한다")
    void homePage_UnauthenticatedUser_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"));
    }

    @Test
    @DisplayName("로그인한 사용자가 홈페이지에 접근할 수 있어야 한다")
    void homePage_AuthenticatedUser_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"));
    }

    @Test
    @DisplayName("회원가입 성공 메시지와 함께 홈페이지에 접근할 수 있어야 한다")
    void homePage_WithSuccessMessage_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."));
    }

    @Test
    @DisplayName("로그인 성공 메시지와 함께 홈페이지에 접근할 수 있어야 한다")
    void homePage_WithLoginSuccessMessage_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/").param("success", "login"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"))
                .andExpect(model().attribute("successMessage", "로그인되었습니다."));
    }
} 