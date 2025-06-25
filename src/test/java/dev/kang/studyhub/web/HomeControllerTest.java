package dev.kang.studyhub.web;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HomeController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HomeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private AtomicInteger emailCounter = new AtomicInteger(1);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("홈페이지 접근 - 로그인하지 않은 사용자")
    @DirtiesContext
    void homePage_NotLoggedIn() throws Exception {
        // given
        createTestStudy("home1@test.com", "테스트 스터디", "테스트 스터디입니다.");

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attributeExists("recentStudies"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("홈페이지 접근 - 로그인한 사용자")
    @DirtiesContext
    void homePage_LoggedIn() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userEmail", "test@test.com"))
                .andExpect(model().attributeExists("recentStudies"));
    }

    @Test
    @DisplayName("홈페이지 접근 - 회원가입 성공 메시지와 함께")
    @DirtiesContext
    void homePage_WithJoinSuccessMessage() throws Exception {
        // given
        createTestStudy("home2@test.com", "테스트 스터디", "테스트 스터디입니다.");

        // when & then
        mockMvc.perform(get("/").param("success", "join"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요."));
    }

    @Test
    @DisplayName("홈페이지 접근 - 로그인 성공 메시지와 함께")
    @DirtiesContext
    void homePage_WithLoginSuccessMessage() throws Exception {
        // given
        createTestStudy("home3@test.com", "테스트 스터디", "테스트 스터디입니다.");

        // when & then
        mockMvc.perform(get("/").param("success", "login"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("successMessage", "로그인되었습니다."));
    }

    @Test
    @DisplayName("홈페이지에서 최근 스터디 목록 조회")
    @DirtiesContext
    void homePage_RecentStudiesList() throws Exception {
        // given
        Study study1 = createTestStudy("study1@test.com", "스터디1", "테스트 스터디 1");
        Study study2 = createTestStudy("study2@test.com", "스터디2", "테스트 스터디 2");
        Study study3 = createTestStudy("study3@test.com", "스터디3", "테스트 스터디 3");

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentStudies"));
    }

    /**
     * 고유한 이메일 생성
     */
    private String generateUniqueEmail() {
        return "test" + emailCounter.getAndIncrement() + "@test.com";
    }

    /**
     * 테스트용 사용자 생성 (고유 이메일 사용)
     */
    private User createTestUser() {
        return createTestUser(generateUniqueEmail());
    }

    /**
     * 테스트용 사용자 생성 (이메일 지정)
     */
    private User createTestUser(String email) {
        // 이미 존재하면 삭제
        userRepository.deleteByEmail(email);
        UserJoinForm form = new UserJoinForm();
        form.setEmail(email);
        form.setPassword("password123");
        form.setName("테스트 사용자");
        form.setUniversity("테스트 대학교");
        form.setMajor("컴퓨터공학과");
        form.setEducationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED);
        
        userService.join(form);
        return userService.findByEmail(email).orElseThrow();
    }

    /**
     * 테스트용 스터디 생성 (고유 이메일 사용)
     */
    private Study createTestStudy() {
        return createTestStudy(generateUniqueEmail(), "테스트 스터디", "테스트 스터디입니다.");
    }

    /**
     * 테스트용 스터디 생성 (제목과 설명 지정)
     */
    private Study createTestStudy(String title, String description) {
        return createTestStudy(generateUniqueEmail(), title, description);
    }

    /**
     * 테스트용 스터디 생성 (이메일, 제목, 설명 지정)
     */
    private Study createTestStudy(String email, String title, String description) {
        User leader = createTestUser(email);
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