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

/**
 * StudyApplicationController 클래스의 웹 계층 테스트
 * 
 * 테스트 대상:
 * - 스터디 신청 처리 (POST /studies/{studyId}/apply)
 * - 스터디 신청 취소 (POST /studies/{studyId}/cancel)
 * - 스터디 신청자 목록 조회 (GET /studies/{studyId}/applications)
 * - 신청 수락 처리 (POST /studies/{studyId}/applications/{applicationId}/accept)
 * - 신청 거절 처리 (POST /studies/{studyId}/applications/{applicationId}/reject)
 * - 내가 신청한 스터디 목록 (GET /studies/applications/my)
 * 
 * 테스트 시나리오:
 * - 정상적인 신청/취소/수락/거절 작업
 * - 권한 검증 (스터디 개설자만 신청자 목록 조회 및 수락/거절 가능)
 * - 예외 상황 처리 (중복 신청, 존재하지 않는 스터디/신청, 권한 없음)
 * - 비즈니스 규칙 검증 (자신의 스터디에 신청 불가)
 */
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

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     * 
     * 설정 내용:
     * - 스터디 개설자 엔티티 생성
     * - 신청자 엔티티 생성
     * - 테스트용 스터디 엔티티 생성
     * - 테스트용 신청 엔티티 생성
     * - UserService Mock 설정 (이메일로 사용자 조회)
     */
    @BeforeEach
    void setUp() {
        // 스터디 개설자 엔티티 생성
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

        // 신청자 엔티티 생성
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

        // 테스트용 스터디 엔티티 생성
        testStudy = Study.builder()
                .title("테스트 스터디")
                .description("테스트 스터디 설명")
                .leader(leaderUser)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();

        // 테스트용 신청 엔티티 생성
        testApplication = StudyApplication.builder()
                .user(testUser)
                .study(testStudy)
                .status(ApplicationStatus.PENDING)
                .build();

        // UserService Mock 설정 - 이메일로 사용자 조회 시 해당 사용자 반환
        when(userService.findByEmail("leader@example.com")).thenReturn(Optional.of(leaderUser));
        when(userService.findByEmail("applicant@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("스터디 신청 처리")
    @WithMockUser(username = "applicant@example.com")
    void applyForStudy() throws Exception {
        // given
        // 스터디 조회, 중복 신청 확인, 신청 처리 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationService.hasApplied(testUser, testStudy)).thenReturn(false);
        when(applicationService.apply(testUser, testStudy)).thenReturn(testApplication);

        // when & then
        // 스터디 신청 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/1/apply"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1?success=applied")); // 성공 메시지와 함께 스터디 상세 페이지로 리다이렉트
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
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "이미 신청한 스터디입니다."));
    }

    @Test
    @DisplayName("스터디 신청 취소 처리")
    @WithMockUser(username = "applicant@example.com")
    void cancelApplication() throws Exception {
        // given
        // 스터디 조회 및 신청 내역 조회 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationRepository.findByUserAndStudy(testUser, testStudy))
                .thenReturn(Optional.of(testApplication));

        // when & then
        // 스터디 신청 취소 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/1/cancel"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1?success=cancelled")); // 성공 메시지와 함께 스터디 상세 페이지로 리다이렉트
    }

    @Test
    @DisplayName("스터디 신청자 목록 조회")
    @WithMockUser(username = "leader@example.com")
    void listApplications() throws Exception {
        // given
        // 스터디 조회 및 신청자 목록 조회 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationService.findByStudy(testStudy)).thenReturn(List.of(testApplication));

        // when & then
        // 스터디 신청자 목록 조회 요청 및 응답 검증
        mockMvc.perform(get("/studies/1/applications"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/applications")) // study/applications 뷰 반환
                .andExpect(model().attributeExists("study")) // study 모델 속성 존재
                .andExpect(model().attributeExists("applications")); // applications 모델 속성 존재
    }

    @Test
    @DisplayName("스터디 개설자가 아닌 사용자가 신청자 목록 조회 시 예외 발생")
    @WithMockUser(username = "applicant@example.com")
    void listApplicationsNotLeader() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(get("/studies/1/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "스터디 개설자만 접근할 수 있습니다."));
    }

    @Test
    @DisplayName("스터디 신청 수락 처리")
    @WithMockUser(username = "leader@example.com")
    void acceptApplication() throws Exception {
        // given
        // 스터디 조회 및 신청 내역 조회 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // when & then
        // 스터디 신청 수락 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/1/applications/1/accept"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1/applications?success=accepted")); // 성공 메시지와 함께 신청자 목록 페이지로 리다이렉트
    }

    @Test
    @DisplayName("스터디 신청 거절 처리")
    @WithMockUser(username = "leader@example.com")
    void rejectApplication() throws Exception {
        // given
        // 스터디 조회 및 신청 내역 조회 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // when & then
        // 스터디 신청 거절 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/1/applications/1/reject"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1/applications?success=rejected")); // 성공 메시지와 함께 신청자 목록 페이지로 리다이렉트
    }

    @Test
    @DisplayName("내가 신청한 스터디 목록 조회")
    @WithMockUser(username = "applicant@example.com")
    void myApplications() throws Exception {
        // given
        // 현재 사용자의 신청 내역 조회 Mock 설정
        when(applicationService.findByUser(testUser)).thenReturn(List.of(testApplication));

        // when & then
        // 내가 신청한 스터디 목록 조회 요청 및 응답 검증
        mockMvc.perform(get("/studies/applications/my"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/my-applications")) // study/my-applications 뷰 반환
                .andExpect(model().attributeExists("applications")); // applications 모델 속성 존재
    }

    @Test
    @DisplayName("존재하지 않는 스터디에 신청 시 예외 발생")
    @WithMockUser(username = "applicant@example.com")
    void applyForNonExistentStudy() throws Exception {
        // given
        when(studyService.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/studies/999/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "스터디를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("스터디 개설자가 자신의 스터디에 신청 시 예외 발생")
    @WithMockUser(username = "leader@example.com")
    void applyForOwnStudy() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(post("/studies/1/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "스터디 개설자는 신청할 수 없습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 신청을 수락할 때 예외 발생")
    @WithMockUser(username = "leader@example.com")
    void acceptNonExistentApplication() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/studies/1/applications/999/accept"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "신청 내역이 존재하지 않습니다."));
    }
} 