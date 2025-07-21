package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.board.entity.Board;
import dev.kang.studyhub.board.entity.Post;
import dev.kang.studyhub.board.entity.PostComment;
import dev.kang.studyhub.board.repository.BoardRepository;
import dev.kang.studyhub.board.repository.PostCommentRepository;
import dev.kang.studyhub.board.repository.PostRepository;
import dev.kang.studyhub.user.entity.User;
import dev.kang.studyhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostCommentRepository 통합 테스트
 *
 * - 게시글 ID로 댓글 목록 조회
 * - 댓글 저장/삭제/조회
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PostCommentRepositoryTest {
    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    private User user;
    private Board board;
    private Post post;
    private PostComment comment1;
    private PostComment comment2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리 (중복/무결성 에러 방지)
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .name("테스트 사용자")
                .username("post_comment_test_user")
                .email("test@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user);

        board = new Board();
        // board name을 매번 다르게 생성하여 중복 방지
        board.setName("커뮤니티_" + UUID.randomUUID());
        board.setDescription("자유로운 소통을 위한 커뮤니티입니다.");
        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);

        post = new Post();
        post.setTitle("댓글 테스트 게시글");
        post.setContent("댓글 테스트 내용");
        post.setUser(user);
        post.setBoard(board);
        post.setCreatedAt(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setDislikeCount(0);
        postRepository.save(post);

        comment1 = new PostComment();
        comment1.setPost(post);
        comment1.setUser(user);
        comment1.setContent("첫 번째 댓글");
        comment1.setCreatedAt(LocalDateTime.now());
        comment1.setLikeCount(0);
        postCommentRepository.save(comment1);

        comment2 = new PostComment();
        comment2.setPost(post);
        comment2.setUser(user);
        comment2.setContent("두 번째 댓글");
        comment2.setCreatedAt(LocalDateTime.now());
        comment2.setLikeCount(0);
        postCommentRepository.save(comment2);
    }

    @Test
    @DisplayName("게시글 ID로 댓글 목록 조회")
    void findByPostId() {
        // when
        List<PostComment> comments = postCommentRepository.findByPostId(post.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(PostComment::getContent)
                .containsExactlyInAnyOrder("첫 번째 댓글", "두 번째 댓글");
    }

    @Test
    @DisplayName("댓글 저장 및 조회")
    void saveAndFind() {
        // when
        PostComment found = postCommentRepository.findById(comment1.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getContent()).isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() {
        // when
        postCommentRepository.delete(comment1);
        List<PostComment> comments = postCommentRepository.findByPostId(post.getId());

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("두 번째 댓글");
    }
} 