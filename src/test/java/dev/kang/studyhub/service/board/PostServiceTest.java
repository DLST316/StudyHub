package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.*;
import dev.kang.studyhub.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * 게시글(Post) 서비스 단위테스트
 * - CRUD, 추천/비추천, 조회수 등 모든 주요 기능 검증
 */
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @InjectMocks
    private PostService postService;

    private Board board;
    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        board = new Board();
        board.setId(1L);
        user = new User();
        user.setId(1L);
        post = new Post();
        post.setId(1L);
        post.setBoard(board);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setDislikeCount(0);
    }

    @Test
    @DisplayName("게시글 목록 페이징 조회")
    void getPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(post);
        given(postRepository.findByBoardId(board.getId(), pageable)).willReturn(new PageImpl<>(posts));
        Page<Post> result = postService.getPosts(board, pageable);
        assertThat(result.getContent()).containsExactly(post);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 성공")
    void getPost_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        Post result = postService.getPost(1L);
        assertThat(result).isEqualTo(post);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 실패(없음)")
    void getPost_fail() {
        given(postRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> postService.getPost(1L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("게시글 저장")
    void savePost() {
        given(postRepository.save(any(Post.class))).willReturn(post);
        Post saved = postService.savePost(post);
        assertThat(saved).isEqualTo(post);
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        willDoNothing().given(postRepository).deleteById(1L);
        postService.deletePost(1L);
        then(postRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("게시글 조회수 증가")
    void increaseView() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        postService.increaseView(1L);
        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Nested
    @DisplayName("추천/비추천 토글")
    class ToggleLike {
        @Test
        @DisplayName("신규 추천")
        void addLike() {
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.empty());
            given(postLikeRepository.save(any(PostLike.class))).willAnswer(inv -> inv.getArgument(0));
            given(postLikeRepository.countLikesByPost(post)).willReturn(1L);
            given(postLikeRepository.countDislikesByPost(post)).willReturn(0L);
            given(postRepository.save(post)).willReturn(post);
            String msg = postService.toggleLike(1L, user, PostLike.LikeType.LIKE);
            assertThat(msg).contains("추천");
            assertThat(post.getLikeCount()).isEqualTo(1);
            assertThat(post.getDislikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("추천 취소")
        void cancelLike() {
            PostLike like = new PostLike();
            like.setType(PostLike.LikeType.LIKE);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(like));
            willDoNothing().given(postLikeRepository).delete(like);
            given(postLikeRepository.countLikesByPost(post)).willReturn(0L);
            given(postLikeRepository.countDislikesByPost(post)).willReturn(0L);
            given(postRepository.save(post)).willReturn(post);
            String msg = postService.toggleLike(1L, user, PostLike.LikeType.LIKE);
            assertThat(msg).contains("취소");
            assertThat(post.getLikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("추천 → 비추천 변경")
        void changeLikeToDislike() {
            PostLike like = new PostLike();
            like.setType(PostLike.LikeType.LIKE);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(like));
            given(postLikeRepository.save(like)).willReturn(like);
            given(postLikeRepository.countLikesByPost(post)).willReturn(0L);
            given(postLikeRepository.countDislikesByPost(post)).willReturn(1L);
            given(postRepository.save(post)).willReturn(post);
            String msg = postService.toggleLike(1L, user, PostLike.LikeType.DISLIKE);
            assertThat(msg).contains("비추천");
            assertThat(post.getLikeCount()).isEqualTo(0);
            assertThat(post.getDislikeCount()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("사용자의 추천/비추천 상태 조회")
    void getUserLikeStatus() {
        PostLike like = new PostLike();
        like.setType(PostLike.LikeType.LIKE);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(like));
        PostLike.LikeType type = postService.getUserLikeStatus(1L, user);
        assertThat(type).isEqualTo(PostLike.LikeType.LIKE);
    }

    @Test
    @DisplayName("게시글의 추천/비추천 수 조회")
    void getLikeCounts() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.countLikesByPost(post)).willReturn(2L);
        given(postLikeRepository.countDislikesByPost(post)).willReturn(1L);
        PostService.LikeCounts counts = postService.getLikeCounts(1L);
        assertThat(counts.getLikeCount()).isEqualTo(2);
        assertThat(counts.getDislikeCount()).isEqualTo(1);
    }
} 