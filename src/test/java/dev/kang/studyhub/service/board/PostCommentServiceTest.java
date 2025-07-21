package dev.kang.studyhub.service.board;

import dev.kang.studyhub.board.entity.Post;
import dev.kang.studyhub.board.entity.PostComment;
import dev.kang.studyhub.board.repository.PostCommentRepository;
import dev.kang.studyhub.board.service.PostCommentService;
import dev.kang.studyhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * 게시글 댓글(PostComment) 서비스 단위테스트
 * - CRUD 등 모든 주요 기능 검증
 */
class PostCommentServiceTest {
    @Mock
    private PostCommentRepository postCommentRepository;
    @InjectMocks
    private PostCommentService postCommentService;

    private Post post;
    private User user;
    private PostComment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        post = new Post();
        post.setId(1L);
        user = new User();
        user.setId(1L);
        comment = new PostComment();
        comment.setId(1L);
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent("댓글 내용");
    }

    @Test
    @DisplayName("댓글 목록 조회")
    void getComments() {
        given(postCommentRepository.findByPostId(post.getId())).willReturn(List.of(comment));
        List<PostComment> result = postCommentService.getComments(post);
        assertThat(result).containsExactly(comment);
    }

    @Test
    @DisplayName("댓글 단건 조회 - 성공")
    void getComment_success() {
        given(postCommentRepository.findById(1L)).willReturn(Optional.of(comment));
        PostComment result = postCommentService.getComment(1L);
        assertThat(result).isEqualTo(comment);
    }

    @Test
    @DisplayName("댓글 단건 조회 - 실패(없음)")
    void getComment_fail() {
        given(postCommentRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> postCommentService.getComment(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("댓글 저장")
    void saveComment() {
        given(postCommentRepository.save(any(PostComment.class))).willReturn(comment);
        PostComment saved = postCommentService.saveComment(comment);
        assertThat(saved).isEqualTo(comment);
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() {
        willDoNothing().given(postCommentRepository).deleteById(1L);
        postCommentService.deleteComment(1L);
        then(postCommentRepository).should().deleteById(1L);
    }
} 