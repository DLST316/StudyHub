package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.study.StudyApplicationService;
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
import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudyApplicationController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StudyApplicationControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyApplicationService studyApplicationService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 신청 - 성공")
    void applyForStudy_Success() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        
        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/apply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + study.getId() + "?success=applied"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 신청 - 스터디장이 신청 시도")
    void applyForStudy_AsLeader() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        
        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 신청 취소 - 성공")
    void cancelApplication_Success() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + study.getId() + "?success=cancelled"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 신청자 목록 조회 - 스터디장")
    void listApplications_AsLeader() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(get("/studies/" + study.getId() + "/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/applications"))
                .andExpect(model().attribute("study", study))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 신청자 목록 조회 - 일반 사용자")
    void listApplications_AsNonLeader() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        
        // when & then
        mockMvc.perform(get("/studies/" + study.getId() + "/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 신청 수락 - 성공")
    void acceptApplication_Success() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        StudyApplication application = studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/applications/" + application.getId() + "/accept"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + study.getId() + "/applications?success=accepted"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("스터디 신청 수락 - 일반 사용자")
    void acceptApplication_AsNonLeader() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        StudyApplication application = studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/applications/" + application.getId() + "/accept"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/400"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("스터디 신청 거절 - 성공")
    void rejectApplication_Success() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        StudyApplication application = studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/applications/" + application.getId() + "/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + study.getId() + "/applications?success=rejected"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("내가 신청한 스터디 목록")
    void myApplications() throws Exception {
        // given
        User leader = createTestUser("test@test.com", "테스트 사용자");
        User applicant = createTestUser("other@test.com", "다른 사용자");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        studyApplicationService.apply(applicant, study);

        // when & then
        mockMvc.perform(get("/studies/applications/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/my-applications"))
                .andExpect(model().attributeExists("applications"));
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser(String email, String name) {
        userRepository.deleteByEmail(email);
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
        return createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
    }

    private Study createTestStudy(String email, String title, String description) {
        User leader = createTestUser(email, "테스트 사용자");
        Study study = Study.builder()
                .title(title)
                .description(description)
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("열정만 있으면 됩니다")
                .deadline(LocalDate.now().plusDays(30))
                .build();
        return studyService.createStudy(study);
    }
} 