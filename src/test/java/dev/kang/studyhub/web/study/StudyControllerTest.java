package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.study.StudyApplicationService;
import dev.kang.studyhub.service.study.StudyCommentService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * StudyController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyApplicationService studyApplicationService;

    @Autowired
    private StudyCommentService studyCommentService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User testUser;
    private User otherUser;
    private User adminUser;
    private Study testStudy;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트 데이터 준비
        testUser = createTestUser("test@test.com", "테스트 사용자");
        otherUser = createTestUser("other@test.com", "다른 사용자");
        adminUser = createTestAdminUser("admin@test.com", "관리자");
        testStudy = createTestStudy();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 목록 페이지 접근")
    void listStudies() throws Exception {
        // given
        createTestStudy("스터디1", "테스트 스터디 1");
        createTestStudy("스터디2", "테스트 스터디 2");

        // when & then
        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/list"))
                .andExpect(model().attributeExists("studies"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 목록 페이지 - 페이징")
    void listStudies_WithPaging() throws Exception {
        // given
        createTestStudy("스터디1", "테스트 스터디 1");
        createTestStudy("스터디2", "테스트 스터디 2");

        // when & then
        mockMvc.perform(get("/studies").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/list"))
                .andExpect(model().attributeExists("studies"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 목록 페이지 - 키워드 검색")
    void listStudies_WithKeyword() throws Exception {
        // given
        createTestStudy("Java 스터디", "Java 학습 스터디");
        createTestStudy("Python 스터디", "Python 학습 스터디");

        // when & then
        mockMvc.perform(get("/studies").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/list"))
                .andExpect(model().attribute("keyword", "Java"))
                .andExpect(model().attributeExists("studies"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 상세 페이지 접근 - 로그인한 사용자")
    void viewStudy_LoggedIn() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/detail"))
                .andExpect(model().attribute("study", testStudy))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 상세 페이지 - 스터디장 접근")
    void viewStudy_AsLeader() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/detail"))
                .andExpect(model().attribute("study", testStudy))
                .andExpect(model().attribute("isLeader", true));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("스터디 상세 페이지 - 어드민 접근")
    void viewStudy_AsAdmin() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/detail"))
                .andExpect(model().attribute("study", testStudy))
                .andExpect(model().attribute("isMember", true)) // 어드민은 댓글을 볼 수 있어야 함
                .andExpect(model().attribute("isLeader", false)); // 스터디장은 아님
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 상세 페이지 - 에러 메시지와 함께")
    void viewStudy_WithError() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/" + testStudy.getId()).param("error", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/detail"))
                .andExpect(model().attribute("errorMessage", "잘못된 요청입니다."));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 생성 폼 페이지 접근")
    void showCreateForm() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 생성 - 성공")
    void createStudy_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/studies/new")
                        .param("title", "새로운 스터디")
                        .param("description", "새로운 스터디 설명")
                        .param("recruitmentLimit", "5")
                        .param("requirement", "열정만 있으면 됩니다")
                        .param("deadline", LocalDate.now().plusDays(30).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/studies/*"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 수정 폼 페이지 접근 - 스터디장")
    void showEditForm_AsLeader() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/edit/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attribute("studyId", testStudy.getId()));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 수정 폼 페이지 접근 - 일반 사용자")
    void showEditForm_AsNonLeader() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/edit/" + testStudy.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "스터디 개설자 또는 관리자만 수정할 수 있습니다."));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("스터디 수정 폼 페이지 접근 - 어드민")
    void showEditForm_AsAdmin() throws Exception {
        // when & then
        mockMvc.perform(get("/studies/edit/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attribute("studyId", testStudy.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 수정 - 성공")
    void updateStudy_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/studies/edit/" + testStudy.getId())
                        .param("title", "수정된 스터디")
                        .param("description", "수정된 스터디 설명")
                        .param("recruitmentLimit", "10")
                        .param("requirement", "수정된 요구사항")
                        .param("deadline", LocalDate.now().plusDays(60).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + testStudy.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 삭제 - 성공")
    void deleteStudy_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/studies/delete/" + testStudy.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 삭제 - 일반 사용자")
    void deleteStudy_AsNonLeader() throws Exception {
        // when & then
        mockMvc.perform(post("/studies/delete/" + testStudy.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "스터디 개설자 또는 관리자만 삭제할 수 있습니다."));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("스터디 삭제 - 어드민")
    void deleteStudy_AsAdmin() throws Exception {
        // when & then
        mockMvc.perform(post("/studies/delete/" + testStudy.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("내가 개설한 스터디 목록")
    void myStudies() throws Exception {
        // given
        createTestStudy("내 스터디1", "내가 만든 스터디 1");
        createTestStudy("내 스터디2", "내가 만든 스터디 2");

        // when & then
        mockMvc.perform(get("/studies/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/my-studies"))
                .andExpect(model().attributeExists("studies"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("스터디 수정 처리 - 어드민")
    void updateStudy_AsAdmin() throws Exception {
        // given
        StudyForm studyForm = new StudyForm();
        studyForm.setTitle("수정된 스터디 제목");
        studyForm.setDescription("수정된 스터디 설명");
        studyForm.setRecruitmentLimit(5);
        studyForm.setRequirement("수정된 요구사항");
        studyForm.setDeadline(LocalDate.now().plusDays(30));
        
        // when & then
        mockMvc.perform(post("/studies/edit/" + testStudy.getId())
                        .param("title", studyForm.getTitle())
                        .param("description", studyForm.getDescription())
                        .param("recruitmentLimit", String.valueOf(studyForm.getRecruitmentLimit()))
                        .param("requirement", studyForm.getRequirement())
                        .param("deadline", studyForm.getDeadline().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + testStudy.getId()));
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser(String email, String name) {
        UserJoinForm form = new UserJoinForm();
        form.setEmail(email);
        form.setPassword("password123");
        form.setName(name);
        form.setUniversity("테스트 대학교");
        form.setMajor("컴퓨터공학과");
        form.setEducationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED);
        
        userService.join(form);
        return userService.findByEmail(email).orElseThrow();
    }

    /**
     * 테스트용 스터디 생성
     */
    private Study createTestStudy() {
        return createTestStudy("테스트 스터디", "테스트 스터디입니다.");
    }

    /**
     * 테스트용 스터디 생성 (제목과 설명 지정)
     */
    private Study createTestStudy(String title, String description) {
        Study study = Study.builder()
                .title(title)
                .description(description)
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("열정만 있으면 됩니다")
                .deadline(LocalDate.now().plusDays(30))
                .build();
        return studyService.createStudy(study);
    }

    private User createTestAdminUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .password("password123")
                .role("ADMIN")
                .build();
        return userRepository.save(user);
    }
} 