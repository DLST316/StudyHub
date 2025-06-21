package dev.kang.studyhub.web.study;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyController.class)
@Import(TestSecurityConfig.class)
class StudyControllerTest {

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
                .id(1L)
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

        // UserService Mock 설정
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("스터디 목록 페이지 조회")
    @WithMockUser
    void listStudies() throws Exception {
        // given
        Page<Study> studyPage = new PageImpl<>(List.of(testStudy));
        when(studyService.findAll(any(Pageable.class))).thenReturn(studyPage);

        // when & then
        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/list"))
                .andExpect(model().attributeExists("studies"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    @DisplayName("스터디 상세 페이지 조회")
    @WithMockUser
    void viewStudy() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(get("/studies/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/detail"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @DisplayName("존재하지 않는 스터디 조회 시 예외 발생")
    @WithMockUser
    void viewStudyNotFound() throws Exception {
        // given
        when(studyService.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/studies/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("스터디 생성 폼 페이지 조회")
    @WithMockUser
    void showCreateForm() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @DisplayName("스터디 생성 처리")
    @WithMockUser(username = "test@example.com")
    void createStudy() throws Exception {
        // given
        when(studyService.createStudy(any(Study.class))).thenReturn(testStudy);

        // when & then
        mockMvc.perform(post("/studies/new")
                        .param("title", "새로운 스터디")
                        .param("description", "새로운 스터디 설명")
                        .param("recruitmentLimit", "5")
                        .param("requirement", "Java 기초")
                        .param("deadline", "2024-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/1"));
    }

    @Test
    @DisplayName("스터디 수정 폼 페이지 조회")
    @WithMockUser(username = "test@example.com")
    void showEditForm() throws Exception {
        // given
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        mockMvc.perform(get("/studies/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("studyId"));
    }

    @Test
    @DisplayName("내가 개설한 스터디 목록 조회")
    @WithMockUser(username = "test@example.com")
    void myStudies() throws Exception {
        // given
        when(studyService.findByLeader(any(User.class))).thenReturn(List.of(testStudy));

        // when & then
        mockMvc.perform(get("/studies/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/my-studies"))
                .andExpect(model().attributeExists("studies"));
    }
} 