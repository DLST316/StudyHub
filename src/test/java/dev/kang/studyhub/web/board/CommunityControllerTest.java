package dev.kang.studyhub.web.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostLike;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.board.BoardService;
import dev.kang.studyhub.service.board.PostCommentService;
import dev.kang.studyhub.service.board.PostService;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommunityController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommunityControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BoardService boardService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostCommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User testUser;
    private Post testPost;
    private Board communityBoard;
    private int emailCounter = 1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트 데이터 준비
        testUser = createTestUser();
        communityBoard = boardService.getCommunityBoard();
        testPost = createTestPost();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("커뮤니티 목록 페이지 접근")
    void communityList() throws Exception {
        // given
        createTestUser("test@test.com");
        createTestPost("테스트 게시글 1", "내용 1");
        createTestPost("테스트 게시글 2", "내용 2");

        // when & then
        mockMvc.perform(get("/community"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("커뮤니티 목록 페이지 - 페이징")
    void communityList_WithPaging() throws Exception {
        // given
        createTestUser("test@test.com");
        createTestPost("테스트 게시글 1", "내용 1");
        createTestPost("테스트 게시글 2", "내용 2");

        // when & then
        mockMvc.perform(get("/community").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 상세 페이지 접근 - 로그인하지 않은 사용자")
    void postDetail_NotLoggedIn() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(get("/community/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-detail"))
                .andExpect(model().attribute("post", testPost))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 상세 페이지 접근 - 로그인한 사용자")
    void postDetail_LoggedIn() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(get("/community/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-detail"))
                .andExpect(model().attribute("post", testPost))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("글쓰기 폼 페이지 접근")
    void writeForm() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(get("/community/write"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 작성 - 성공")
    void writePost_Success() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(post("/community/write")
                        .param("title", "새로운 게시글")
                        .param("content", "새로운 게시글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 작성 - 유효성 검사 실패")
    void writePost_ValidationError() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(post("/community/write")
                        .param("title", "")  // 제목이 비어있음
                        .param("content", "내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 추천 - 성공")
    void toggleLike_Success() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/like"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 비추천 - 성공")
    void toggleDislike_Success() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/dislike"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 작성 - 성공")
    void addComment_Success() throws Exception {
        createTestUser("test@test.com");
        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/comment")
                        .param("content", "새로운 댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_Success() throws Exception {
        createTestUser("test@test.com");
        // given
        PostComment comment = createTestComment();

        // when & then
        mockMvc.perform(post("/community/comment/" + comment.getId() + "/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 로그인하지 않은 사용자")
    void deleteComment_NotLoggedIn() throws Exception {
        // given
        PostComment comment = createTestComment();

        // when & then
        mockMvc.perform(post("/community/comment/" + comment.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())  // 302 Redirect to login
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("댓글 삭제 - 다른 사용자")
    void deleteComment_OtherUser() throws Exception {
        // given
        createTestUser("other@test.com");
        PostComment comment = createTestComment();

        // when & then
        mockMvc.perform(post("/community/comment/" + comment.getId() + "/delete"))
                .andExpect(status().isForbidden())  // 403 Forbidden
                .andExpect(content().string("댓글 작성자만 삭제할 수 있습니다."));
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        return createTestUser(generateUniqueEmail());
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
        return userService.findByEmail(email).orElseThrow();
    }

    private String generateUniqueEmail() {
        return "test" + (emailCounter++) + "@test.com";
    }

    /**
     * 테스트용 게시글 생성
     */
    private Post createTestPost() {
        return createTestPost("테스트 게시글", "테스트 게시글 내용입니다.");
    }

    /**
     * 테스트용 게시글 생성 (제목과 내용 지정)
     */
    private Post createTestPost(String title, String content) {
        Post post = new Post();
        post.setBoard(communityBoard);
        post.setUser(testUser);
        post.setTitle(title);
        post.setContent(content);
        return postService.savePost(post);
    }

    private Post createTestPost(String email, String title, String content) {
        User user = createTestUser(email);
        Post post = new Post();
        post.setBoard(communityBoard);
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        return postService.savePost(post);
    }

    /**
     * 테스트용 댓글 생성
     */
    private PostComment createTestComment() {
        return createTestComment("test@test.com");
    }

    private PostComment createTestComment(String email) {
        PostComment comment = new PostComment();
        comment.setPost(testPost);
        comment.setUser(createTestUser(email));
        comment.setContent("테스트 댓글입니다.");
        return commentService.saveComment(comment);
    }
} 