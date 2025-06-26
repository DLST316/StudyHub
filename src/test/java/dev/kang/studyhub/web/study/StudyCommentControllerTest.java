package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyComment;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.study.StudyCommentService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.service.study.StudyApplicationService;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudyCommentController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StudyCommentControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyCommentService studyCommentService;

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
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 작성 - 성공")
    void createComment_Success() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        
        // when & then
        mockMvc.perform(post("/studies/" + study.getId() + "/comments")
                        .param("content", "새로운 댓글입니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/" + study.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 작성 - 존재하지 않는 스터디")
    void createComment_NonExistentStudy() throws Exception {
        // given
        createTestUser("test@test.com");
        
        // when & then
        mockMvc.perform(post("/studies/99999/comments")
                        .param("content", "새로운 댓글입니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studies/99999?error=invalid"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 수정 - 성공")
    void updateComment_Success() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        Study study = createTestStudy(user, "테스트 스터디", "테스트 스터디입니다.");
        StudyComment comment = createTestComment(user, study);

        // when & then
        mockMvc.perform(put("/studies/" + study.getId() + "/comments/" + comment.getId())
                        .param("content", "수정된 댓글입니다."))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("댓글 수정 - 다른 사용자")
    void updateComment_OtherUser() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        User otherUser = createTestUser("other@test.com");
        Study study = createTestStudy(user, "테스트 스터디", "테스트 스터디입니다.");
        StudyComment comment = createTestComment(user, study);

        // when & then
        mockMvc.perform(put("/studies/" + study.getId() + "/comments/" + comment.getId())
                        .param("content", "수정된 댓글입니다."))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error:")));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_Success() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        Study study = createTestStudy(user, "테스트 스터디", "테스트 스터디입니다.");
        StudyComment comment = createTestComment(user, study);

        // when & then
        mockMvc.perform(delete("/studies/" + study.getId() + "/comments/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("댓글 삭제 - 다른 사용자")
    void deleteComment_OtherUser() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        User otherUser = createTestUser("other@test.com");
        Study study = createTestStudy(user, "테스트 스터디", "테스트 스터디입니다.");
        StudyComment comment = createTestComment(user, study);

        // when & then
        mockMvc.perform(delete("/studies/" + study.getId() + "/comments/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error:")));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 삭제 - 존재하지 않는 댓글")
    void deleteComment_NonExistentComment() throws Exception {
        // given
        User user = createTestUser("test@test.com");
        Study study = createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
        
        // when & then
        mockMvc.perform(delete("/studies/" + study.getId() + "/comments/99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error:")));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("댓글 삭제 - 어드민 성공")
    void deleteComment_AdminSuccess() throws Exception {
        // given
        User admin = createTestUser("admin@test.com");
        User user = createTestUser("test@test.com");
        Study study = createTestStudy(user, "테스트 스터디", "테스트 스터디입니다.");
        StudyComment comment = createTestComment(user, study);

        // when & then
        mockMvc.perform(delete("/studies/" + study.getId() + "/comments/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        return createTestUser("test@test.com");
    }

    private User createTestUser(String email) {
        userRepository.deleteByEmail(email);
        UserJoinForm form = new UserJoinForm();
        form.setEmail(email);
        form.setPassword("password123");
        form.setName("테스트 사용자");
        form.setUniversity("테스트 대학교");
        form.setMajor("컴퓨터공학과");
        form.setEducationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED);
        userService.join(form);
        User user = userService.findByEmail(email).orElseThrow();
        // 어드민 계정이면 role을 ADMIN으로 변경
        if (email.equals("admin@test.com")) {
            user.setRole("ADMIN");
            userRepository.save(user);
        }
        return user;
    }

    /**
     * 테스트용 스터디 생성
     */
    private Study createTestStudy() {
        return createTestStudy("test@test.com", "테스트 스터디", "테스트 스터디입니다.");
    }

    private Study createTestStudy(String email, String title, String description) {
        User leader = createTestUser(email);
        return createTestStudy(leader, title, description);
    }

    private Study createTestStudy(User leader, String title, String description) {
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

    /**
     * 테스트용 댓글 생성
     */
    private StudyComment createTestComment() {
        return createTestComment("test@test.com");
    }

    private StudyComment createTestComment(String email) {
        Study study = createTestStudy(email, "테스트 스터디", "테스트 스터디입니다.");
        return createTestComment(email, study);
    }

    private StudyComment createTestComment(String email, Study study) {
        User user = createTestUser(email);
        return createTestComment(user, study);
    }

    private StudyComment createTestComment(User user, Study study) {
        // 스터디 개설자만 댓글을 작성하도록 제한
        if (!study.isLeader(user)) {
            throw new IllegalStateException("스터디 개설자만 댓글을 작성할 수 있습니다.");
        }
        return studyCommentService.createComment("테스트 댓글입니다.", user, study);
    }
} 