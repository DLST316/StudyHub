package dev.kang.studyhub.web.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostLike;
import dev.kang.studyhub.domain.board.BoardRepository;
import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.board.BoardService;
import dev.kang.studyhub.service.board.PostCommentService;
import dev.kang.studyhub.service.board.PostService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommunityController 통합 테스트
 * 실제 DB와 서비스를 사용하여 비즈니스 로직을 철저히 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
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
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User testUser;
    private Board communityBoard;
    private Post testPost;
    private PostComment testComment;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        boardRepository.deleteAll();
        userRepository.deleteAll();
        
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트 데이터 준비 - 실제 DB에 저장
        testUser = userRepository.save(createTestUser());
        communityBoard = boardRepository.save(createTestBoard());
        testPost = postService.createPost(communityBoard, testUser, "테스트 게시글", "테스트 내용");
        testComment = commentService.createComment(testPost, testUser, "테스트 댓글");
    }

    /**
     * [인증 없이 접근] 커뮤니티 목록 페이지 접근 시 로그인 페이지로 리다이렉트되는지 검증
     */
    @Test
    @DisplayName("[비회원] 커뮤니티 목록 접근 시 로그인 페이지로 이동")
    void communityList_Unauthenticated() throws Exception {
        mockMvc.perform(get("/community"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    /**
     * [인증] 커뮤니티 목록 페이지 접근 (정상)
     */
    @Test
    @DisplayName("[회원] 커뮤니티 목록 페이지 접근")
    void communityList() throws Exception {
        // given - 추가 게시글 생성
        Post additionalPost = postService.createPost(communityBoard, testUser, "추가 게시글", "추가 내용");

        // when & then - 실제 로그인된 사용자로 테스트
        mockMvc.perform(get("/community")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));

        // 비즈니스 로직 검증: 게시글이 실제로 DB에 저장되었는지 확인
        List<Post> savedPosts = postRepository.findByBoardId(communityBoard.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        assertThat(savedPosts).hasSize(2);
        assertThat(savedPosts).extracting("title").contains("테스트 게시글", "추가 게시글");
    }

    /**
     * [인증] 커뮤니티 목록 페이지 - 페이징
     */
    @Test
    @DisplayName("[회원] 커뮤니티 목록 페이지 - 페이징")
    void communityList_WithPaging() throws Exception {
        // given - 여러 게시글 생성
        for (int i = 1; i <= 5; i++) {
            postService.createPost(communityBoard, testUser, "게시글 " + i, "내용 " + i);
        }

        // when & then - 실제 로그인된 사용자로 테스트
        mockMvc.perform(get("/community").param("page", "0")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));

        // 비즈니스 로직 검증: 페이징이 올바르게 작동하는지 확인
        Page<Post> postsPage = postService.getPosts(communityBoard, PageRequest.of(0, 10));
        assertThat(postsPage.getContent()).hasSize(6); // 기존 1개 + 새로 생성한 5개
    }

    /**
     * [인증 없이 접근] 게시글 상세 접근 시 로그인 페이지로 리다이렉트
     */
    @Test
    @DisplayName("[비회원] 게시글 상세 접근 시 로그인 페이지로 이동")
    void postDetail_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/community/post/" + testPost.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    /**
     * [인증] 게시글 상세 접근 (정상)
     */
    @Test
    @DisplayName("[회원] 게시글 상세 접근")
    void postDetail_LoggedIn() throws Exception {
        mockMvc.perform(get("/community/post/" + testPost.getId())
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("comments"));

        // 비즈니스 로직 검증: 댓글이 올바르게 조회되는지 확인
        List<PostComment> comments = commentService.findCommentsByPost(testPost);
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("테스트 댓글");
    }

    /**
     * [인증] 글쓰기 폼 페이지 접근
     */
    @Test
    @DisplayName("[회원] 글쓰기 폼 페이지 접근")
    void writeForm() throws Exception {
        mockMvc.perform(get("/community/write")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"))
                .andExpect(model().attributeExists("postForm"));
    }

    /**
     * [인증] 게시글 작성 - 성공
     */
    @Test
    @DisplayName("[회원] 게시글 작성 - 성공")
    void writePost_Success() throws Exception {
        // when & then - 실제 로그인된 사용자로 테스트
        mockMvc.perform(post("/community/write")
                        .param("title", "새로운 게시글")
                        .param("content", "새로운 게시글 내용")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community"));

        // 비즈니스 로직 검증: 게시글이 실제로 DB에 저장되었는지 확인
        List<Post> allPosts = postRepository.findByBoardId(communityBoard.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        assertThat(allPosts).extracting("title").contains("새로운 게시글");
        
        Post newPost = allPosts.stream()
                .filter(p -> "새로운 게시글".equals(p.getTitle()))
                .findFirst()
                .orElseThrow();
        assertThat(newPost.getContent()).isEqualTo("새로운 게시글 내용");
        assertThat(newPost.getViewCount()).isEqualTo(0);
        assertThat(newPost.getLikeCount()).isEqualTo(0);
    }

    /**
     * [인증] 게시글 작성 - 유효성 검사 실패
     */
    @Test
    @DisplayName("[회원] 게시글 작성 - 유효성 검사 실패")
    void writePost_ValidationError() throws Exception {
        mockMvc.perform(post("/community/write")
                        .param("title", "")  // 제목 누락
                        .param("content", "내용")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * [인증] 게시글 작성 - 내용이 비어있는 경우
     */
    @Test
    @DisplayName("[회원] 게시글 작성 - 내용이 비어있는 경우")
    void writePost_EmptyContent() throws Exception {
        mockMvc.perform(post("/community/write")
                        .param("title", "제목")
                        .param("content", "")  // 내용 누락
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"))
                .andExpect(model().hasErrors());
    }

    /**
     * [인증] 게시글 추천 - 성공
     */
    @Test
    @DisplayName("[회원] 게시글 추천 - 성공")
    void toggleLike_Success() throws Exception {
        // given - 초기 추천 수 확인
        int initialLikeCount = testPost.getLikeCount();

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/like")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));

        // 비즈니스 로직 검증: 추천 수가 증가했는지 확인
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getLikeCount()).isGreaterThan(initialLikeCount);
    }

    /**
     * [인증] 게시글 비추천 - 성공
     */
    @Test
    @DisplayName("[회원] 게시글 비추천 - 성공")
    void toggleDislike_Success() throws Exception {
        // given - 초기 비추천 수 확인
        int initialDislikeCount = testPost.getDislikeCount();

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/dislike")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));

        // 비즈니스 로직 검증: 비추천 수가 증가했는지 확인
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getDislikeCount()).isGreaterThan(initialDislikeCount);
    }

    /**
     * [인증] 댓글 작성 - 성공
     */
    @Test
    @DisplayName("[회원] 댓글 작성 - 성공")
    void addComment_Success() throws Exception {
        // given - 초기 댓글 수 확인
        int initialCommentCount = commentService.findCommentsByPost(testPost).size();

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/comment")
                        .param("content", "새로운 댓글")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));

        // 비즈니스 로직 검증: 댓글이 실제로 DB에 저장되었는지 확인
        List<PostComment> comments = commentService.findCommentsByPost(testPost);
        assertThat(comments).hasSize(initialCommentCount + 1);
        assertThat(comments).extracting("content").contains("새로운 댓글");
    }

    /**
     * [인증] 댓글 삭제 - 성공
     */
    @Test
    @DisplayName("[회원] 댓글 삭제 - 성공")
    void deleteComment_Success() throws Exception {
        // given - 추가 댓글 생성
        PostComment additionalComment = commentService.createComment(testPost, testUser, "삭제할 댓글");
        int initialCommentCount = commentService.findCommentsByPost(testPost).size();

        // when & then
        mockMvc.perform(post("/community/comment/" + additionalComment.getId() + "/delete")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글이 삭제되었습니다."));

        // 비즈니스 로직 검증: 댓글이 실제로 DB에서 삭제되었는지 확인
        List<PostComment> comments = commentService.findCommentsByPost(testPost);
        assertThat(comments).hasSize(initialCommentCount - 1);
        assertThat(comments).extracting("content").doesNotContain("삭제할 댓글");
    }

    /**
     * [인증] 게시글 삭제 - 작성자 성공
     */
    @Test
    @DisplayName("[회원] 게시글 삭제 - 작성자 성공")
    void deletePost_Success() throws Exception {
        // given - 삭제할 게시글 생성
        Post postToDelete = postService.createPost(communityBoard, testUser, "삭제할 게시글", "삭제할 내용");
        int initialPostCount = postRepository.findByBoardId(communityBoard.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent().size();

        // when & then
        mockMvc.perform(post("/community/post/" + postToDelete.getId() + "/delete")
                        .with(user(testUser.getEmail()).password("password").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community?success=deleted"));

        // 비즈니스 로직 검증: 게시글이 실제로 DB에서 삭제되었는지 확인
        List<Post> posts = postRepository.findByBoardId(communityBoard.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        assertThat(posts).hasSize(initialPostCount - 1);
        assertThat(posts).extracting("title").doesNotContain("삭제할 게시글");
    }

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        return User.builder()
                .name("테스트 사용자")
                .email("test@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("USER")
                .build();
    }

    /**
     * 테스트용 어드민 사용자 생성
     */
    private User createAdminUser() {
        return User.builder()
                .name("어드민")
                .email("admin@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("ADMIN")
                .build();
    }

    /**
     * 테스트용 다른 사용자 생성
     */
    private User createOtherUser() {
        return User.builder()
                .name("다른 사용자")
                .email("other@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("USER")
                .build();
    }

    /**
     * 테스트용 게시판 생성
     */
    private Board createTestBoard() {
        Board board = new Board();
        board.setName("커뮤니티");
        board.setDescription("자유로운 소통 공간");
        board.setCreatedAt(LocalDateTime.now());
        return board;
    }
} 