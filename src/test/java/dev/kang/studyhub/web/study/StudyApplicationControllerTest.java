package dev.kang.studyhub.web.study;

import dev.kang.studyhub.config.TestSecurityConfig;
import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.study.repository.StudyApplicationRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.study.StudyApplicationService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyApplicationController.class)
@Import(TestSecurityConfig.class)
class StudyApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyService studyService;

    @MockBean
    private StudyApplicationService applicationService;

    @MockBean
    private StudyApplicationRepository applicationRepository;

    @MockBean
    private UserService userService;

    private User testUser;
    private User leaderUser;
    private Study testStudy;
    private StudyApplication testApplication;

    @BeforeEach
    void setUp() {
        leaderUser = User.builder()
                .id(1L)
                .name("스터디장")
                .email("leader@example.com")
                .password("encodedPassword")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        testUser = User.builder()
                .id(2L)
                .name("신청자")
                .email("applicant@example.com")
                .password("encodedPassword")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        testStudy = Study.builder()
                .title("테스트 스터디")
                .description("테스트 스터디 설명")
                .leader(leaderUser)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();

        testApplication = StudyApplication.builder()
                .user(testUser)
                .study(testStudy)
                .status(ApplicationStatus.PENDING)
                .build();

        // UserService Mock 설정
        when(userService.findByEmail("leader@example.com")).thenReturn(Optional.of(leaderUser));
        when(userService.findByEmail("applicant@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("스터디 신청 처리")
    @WithMockUser(username = "applicant@example.com")
    void applyForStudy() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationService.hasApplied(testUser, testStudy)).thenReturn(false);
        when(applicationService.apply(testUser, testStudy)).thenReturn(testApplication);

        // when & then
        mockMvc.perform(post("/studies/1/apply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/1?success=applied"));
    }

    @Test
    @DisplayName("이미 신청한 스터디에 재신청 시 예외 발생")
    @WithMockUser(username = "applicant@example.com")
    void applyForStudyAlreadyApplied() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationService.hasApplied(testUser, testStudy)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/studies/1/apply"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("스터디 신청 취소 처리")
    @WithMockUser(username = "applicant@example.com")
    void cancelApplication() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationRepository.findByUserAndStudy(testUser, testStudy))
                .thenReturn(Optional.of(testApplication));

        // when & then
        mockMvc.perform(post("/studies/1/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/1?success=cancelled"));
    }

    @Test
    @DisplayName("스터디 신청자 목록 조회")
    @WithMockUser(username = "leader@example.com")
    void listApplications() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationService.findByStudy(testStudy)).thenReturn(List.of(testApplication));

        // when & then
        mockMvc.perform(get("/studies/1/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/applications"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    @DisplayName("스터디 개설자가 아닌 사용자가 신청자 목록 조회 시 예외 발생")
    @WithMockUser(username = "applicant@example.com")
    void listApplicationsNotLeader() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(get("/studies/1/applications"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("스터디 신청 수락 처리")
    @WithMockUser(username = "leader@example.com")
    void acceptApplication() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(post("/studies/1/applications/1/accept"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/1/applications?success=accepted"));
    }

    @Test
    @DisplayName("스터디 신청 거절 처리")
    @WithMockUser(username = "leader@example.com")
    void rejectApplication() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(post("/studies/1/applications/1/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/1/applications?success=rejected"));
    }

    @Test
    @DisplayName("내가 신청한 스터디 목록 조회")
    @WithMockUser(username = "applicant@example.com")
    void myApplications() throws Exception {
        // given
        when(applicationService.findByUser(testUser)).thenReturn(List.of(testApplication));

        // when & then
        mockMvc.perform(get("/studies/applications/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/my-applications"))
                .andExpect(model().attributeExists("applications"));
    }
} 