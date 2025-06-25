package dev.kang.studyhub.web;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController 단위 테스트
 * 
 * 테스트 대상:
 * - 홈페이지 컨트롤러 엔드포인트
 * - 로그인 상태에 따른 뷰 렌더링
 * - 최근 스터디 목록 조회
 * 
 * 테스트 시나리오:
 * - 로그인하지 않은 사용자 접근
 * - 로그인한 사용자 접근
 * - 성공 메시지와 함께 접근
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private StudyService studyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(homeController)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("홈페이지 접근 - 로그인하지 않은 사용자")
    void homePage_NotLoggedIn() throws Exception {
        // given
        List<Study> recentStudies = List.of(
            createMockStudy("테스트 스터디 1"),
            createMockStudy("테스트 스터디 2")
        );
        when(studyService.findRecentStudies()).thenReturn(recentStudies);

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attribute("recentStudies", recentStudies));

        verify(studyService, times(1)).findRecentStudies();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("홈페이지 접근 - 로그인한 사용자")
    void homePage_LoggedIn() throws Exception {
        // given
        List<Study> recentStudies = List.of(
            createMockStudy("테스트 스터디 1"),
            createMockStudy("테스트 스터디 2")
        );
        when(studyService.findRecentStudies()).thenReturn(recentStudies);

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userEmail", "test@test.com"))
                .andExpect(model().attribute("recentStudies", recentStudies));

        verify(studyService, times(1)).findRecentStudies();
    }

    @Test
    @DisplayName("홈페이지 접근 - 회원가입 성공 메시지와 함께")
    void homePage_WithJoinSuccessMessage() throws Exception {
        // given
        List<Study> recentStudies = List.of(createMockStudy("테스트 스터디"));
        when(studyService.findRecentStudies()).thenReturn(recentStudies);

        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."))
                .andExpect(model().attribute("recentStudies", recentStudies));

        verify(studyService, times(1)).findRecentStudies();
    }

    @Test
    @DisplayName("홈페이지 접근 - 로그인 성공 메시지와 함께")
    void homePage_WithLoginSuccessMessage() throws Exception {
        // given
        List<Study> recentStudies = List.of(createMockStudy("테스트 스터디"));
        when(studyService.findRecentStudies()).thenReturn(recentStudies);

        // when & then
        mockMvc.perform(get("/").param("success", "login"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "로그인되었습니다."))
                .andExpect(model().attribute("recentStudies", recentStudies));

        verify(studyService, times(1)).findRecentStudies();
    }

    @Test
    @DisplayName("홈페이지에서 최근 스터디 목록 조회")
    void homePage_RecentStudiesList() throws Exception {
        // given
        List<Study> recentStudies = List.of(
            createMockStudy("스터디1"),
            createMockStudy("스터디2"),
            createMockStudy("스터디3")
        );
        when(studyService.findRecentStudies()).thenReturn(recentStudies);

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("recentStudies", recentStudies));

        verify(studyService, times(1)).findRecentStudies();
    }

    /**
     * 테스트용 스터디 모의 객체 생성
     */
    private Study createMockStudy(String title) {
        User leader = User.builder()
                .id(1L)
                .name("테스트 리더")
                .email("leader@test.com")
                .build();

        return Study.builder()
                .title(title)
                .description("테스트 스터디입니다.")
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("열정만 있으면 됩니다")
                .deadline(LocalDate.now().plusDays(30))
                .build();
    }
} 