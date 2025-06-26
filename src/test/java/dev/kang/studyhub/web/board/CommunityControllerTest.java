package dev.kang.studyhub.web.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostLike;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.board.BoardService;
import dev.kang.studyhub.service.board.PostCommentService;
import dev.kang.studyhub.service.board.PostService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommunityController 단위 테스트
 * 
 * 테스트 대상:
 * - 커뮤니티 게시판 컨트롤러 엔드포인트
 * - 게시글 CRUD 기능
 * - 댓글 기능
 * - 추천/비추천 기능
 * 
 * 테스트 시나리오:
 * - 정상적인 API 호출
 * - 유효성 검사
 * - 권한 검증
 */
@ExtendWith(MockitoExtension.class)
class CommunityControllerTest {

    @Mock
    private BoardService boardService;

    @Mock
    private PostService postService;

    @Mock
    private PostCommentService commentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommunityController communityController;

    private MockMvc mockMvc;
    private User testUser;
    private Post testPost;
    private Board communityBoard;
    private PostComment testComment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(communityController)
                .build();

        // 테스트 데이터 준비
        testUser = createMockUser();
        communityBoard = createMockBoard();
        testPost = createMockPost();
        testComment = createMockComment();
    }

    @Test
    @DisplayName("커뮤니티 목록 페이지 접근")
    void communityList() throws Exception {
        // given
        Page<Post> postPage = new PageImpl<>(List.of(testPost));
        when(postService.getPosts(any(), any(PageRequest.class))).thenReturn(postPage);
        when(boardService.getCommunityBoard()).thenReturn(communityBoard);

        // when & then
        mockMvc.perform(get("/community"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));

        verify(postService, times(1)).getPosts(any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("커뮤니티 목록 페이지 - 페이징")
    void communityList_WithPaging() throws Exception {
        // given
        Page<Post> postPage = new PageImpl<>(List.of(testPost));
        when(postService.getPosts(any(), any(PageRequest.class))).thenReturn(postPage);
        when(boardService.getCommunityBoard()).thenReturn(communityBoard);

        // when & then
        mockMvc.perform(get("/community").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/community"))
                .andExpect(model().attributeExists("posts"));

        verify(postService, times(1)).getPosts(any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("게시글 상세 페이지 접근 - 로그인하지 않은 사용자")
    void postDetail_NotLoggedIn() throws Exception {
        // given
        when(postService.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentService.findCommentsByPost(testPost)).thenReturn(List.of(testComment));

        // when & then
        mockMvc.perform(get("/community/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-detail"))
                .andExpect(model().attribute("post", testPost))
                .andExpect(model().attributeExists("comments"));

        verify(postService, times(1)).findById(testPost.getId());
        verify(commentService, times(1)).findCommentsByPost(testPost);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 상세 페이지 접근 - 로그인한 사용자")
    void postDetail_LoggedIn() throws Exception {
        // given
        when(postService.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentService.findCommentsByPost(testPost)).thenReturn(List.of(testComment));
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        // when & then
        mockMvc.perform(get("/community/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-detail"))
                .andExpect(model().attribute("post", testPost))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("currentUser"));

        verify(postService, times(1)).findById(testPost.getId());
        verify(commentService, times(1)).findCommentsByPost(testPost);
        verify(userService, times(1)).findByEmail("test@test.com");
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("글쓰기 폼 페이지 접근")
    void writeForm() throws Exception {
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
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(boardService.getCommunityBoard()).thenReturn(communityBoard);
        when(postService.createPost(any(), any(), any(), any())).thenReturn(testPost);

        // when & then
        mockMvc.perform(post("/community/write")
                        .param("title", "새로운 게시글")
                        .param("content", "새로운 게시글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community"));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(boardService, times(1)).getCommunityBoard();
        verify(postService, times(1)).createPost(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 작성 - 유효성 검사 실패")
    void writePost_ValidationError() throws Exception {
        // when & then
        mockMvc.perform(post("/community/write")
                        .param("title", "")  // 제목이 비어있음
                        .param("content", "내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"));

        verify(postService, never()).createPost(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 작성 - 내용이 비어있는 경우")
    void writePost_EmptyContent() throws Exception {
        // when & then
        mockMvc.perform(post("/community/write")
                        .param("title", "제목")
                        .param("content", ""))  // 내용이 비어있음
                .andExpect(status().isOk())
                .andExpect(view().name("community/post-form"));

        verify(postService, never()).createPost(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 추천 - 성공")
    void toggleLike_Success() throws Exception {
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(postService.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postService.toggleLike(any(), any(), any())).thenReturn("추천되었습니다.");

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(content().string("추천되었습니다."));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(postService, times(1)).findById(testPost.getId());
        verify(postService, times(1)).toggleLike(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 비추천 - 성공")
    void toggleDislike_Success() throws Exception {
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(postService.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postService.toggleDislike(any(), any(), any())).thenReturn("비추천되었습니다.");

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/dislike"))
                .andExpect(status().isOk())
                .andExpect(content().string("비추천되었습니다."));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(postService, times(1)).findById(testPost.getId());
        verify(postService, times(1)).toggleDislike(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 작성 - 성공")
    void addComment_Success() throws Exception {
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(postService.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentService.createComment(any(), any(), any())).thenReturn(testComment);

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/comment")
                        .param("content", "새로운 댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId()));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(postService, times(1)).findById(testPost.getId());
        verify(commentService, times(1)).createComment(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_Success() throws Exception {
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(commentService.getComment(testComment.getId())).thenReturn(testComment);

        // when & then
        mockMvc.perform(post("/community/comment/" + testComment.getId() + "/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글이 삭제되었습니다."));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(commentService, times(1)).getComment(testComment.getId());
        verify(commentService, times(1)).deleteComment(testComment.getId());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("게시글 삭제 - 작성자 성공")
    void deletePost_Success() throws Exception {
        // given
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(postService.getPost(testPost.getId())).thenReturn(testPost);

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community?success=deleted"));

        verify(userService, times(1)).findByEmail("test@test.com");
        verify(postService, times(1)).getPost(testPost.getId());
        verify(postService, times(1)).deletePost(testPost.getId());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("게시글 삭제 - 어드민 성공")
    void deletePost_AdminSuccess() throws Exception {
        // given
        User adminUser = createMockAdminUser();
        when(userService.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(postService.getPost(testPost.getId())).thenReturn(testPost);

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community?success=deleted"));

        verify(userService, times(1)).findByEmail("admin@test.com");
        verify(postService, times(1)).getPost(testPost.getId());
        verify(postService, times(1)).deletePost(testPost.getId());
    }

    @Test
    @WithMockUser(username = "other@test.com")
    @DisplayName("게시글 삭제 - 권한 없음")
    void deletePost_NoPermission() throws Exception {
        // given
        User otherUser = createMockOtherUser();
        when(userService.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(postService.getPost(testPost.getId())).thenReturn(testPost);

        // when & then
        mockMvc.perform(post("/community/post/" + testPost.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community/post/" + testPost.getId() + "?error=permission"));

        verify(userService, times(1)).findByEmail("other@test.com");
        verify(postService, times(1)).getPost(testPost.getId());
        verify(postService, never()).deletePost(any());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    @DisplayName("댓글 삭제 - 어드민 성공")
    void deleteComment_AdminSuccess() throws Exception {
        // given
        User adminUser = createMockAdminUser();
        when(userService.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(commentService.getComment(testComment.getId())).thenReturn(testComment);

        // when & then
        mockMvc.perform(post("/community/comment/" + testComment.getId() + "/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글이 삭제되었습니다."));

        verify(userService, times(1)).findByEmail("admin@test.com");
        verify(commentService, times(1)).getComment(testComment.getId());
        verify(commentService, times(1)).deleteComment(testComment.getId());
    }

    /**
     * 테스트용 사용자 모의 객체 생성
     */
    private User createMockUser() {
        return User.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@test.com")
                .password("encoded")
                .university("테스트대")
                .major("컴퓨터")
                .role("USER")
                .isBlocked(false)
                .build();
    }

    /**
     * 테스트용 게시판 모의 객체 생성
     */
    private Board createMockBoard() {
        Board board = new Board();
        board.setId(1L);
        board.setName("커뮤니티");
        board.setDescription("자유게시판");
        return board;
    }

    /**
     * 테스트용 게시글 모의 객체 생성
     */
    private Post createMockPost() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("테스트 게시글");
        post.setContent("테스트 게시글 내용");
        post.setUser(testUser);
        post.setBoard(communityBoard);
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    /**
     * 테스트용 댓글 모의 객체 생성
     */
    private PostComment createMockComment() {
        PostComment comment = new PostComment();
        comment.setId(1L);
        comment.setPost(testPost);
        comment.setUser(testUser);
        comment.setContent("테스트 댓글");
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    private User createMockAdminUser() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setName("관리자");
        adminUser.setRole("ADMIN");
        return adminUser;
    }

    private User createMockOtherUser() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setEmail("other@test.com");
        otherUser.setName("다른사용자");
        otherUser.setRole("USER");
        return otherUser;
    }
} 