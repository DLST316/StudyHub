package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BoardRepository 통합 테스트
 *
 * - 게시판명으로 게시판 조회
 * - 게시판 저장/삭제/조회
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BoardRepositoryTest {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserRepository userRepository;

    private Board board;
    private User user;
    private Post post;
    private PostLike postLike;
    private PostComment postComment;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리 (중복/무결성 에러 방지)
        postCommentRepository.deleteAll();
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        user = User.builder()
                .name("테스트 사용자")
                .username("board_test_user")
                .email("test@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user);

        // 게시판 생성
        board = new Board();
        board.setName("커뮤니티_" + UUID.randomUUID());
        board.setDescription("자유로운 소통을 위한 커뮤니티입니다.");
        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);

        // 테스트용 게시글 생성
        post = new Post();
        post.setTitle("테스트 게시글");
        post.setContent("테스트 내용");
        post.setUser(user);
        post.setBoard(board);
        post.setCreatedAt(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setDislikeCount(0);
        postRepository.save(post);

        // 테스트용 게시글 추천 생성
        postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);
        postLike.setType(PostLike.LikeType.LIKE);
        postLike.setCreatedAt(LocalDateTime.now());
        postLikeRepository.save(postLike);

        // 테스트용 게시글 댓글 생성
        postComment = new PostComment();
        postComment.setPost(post);
        postComment.setUser(user);
        postComment.setContent("테스트 댓글");
        postComment.setCreatedAt(LocalDateTime.now());
        postComment.setLikeCount(0);
        postCommentRepository.save(postComment);
    }

    @Test
    @DisplayName("게시판명으로 게시판 조회")
    void findByName() {
        // when
        Board found = boardRepository.findByName(board.getName());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(board.getName());
        assertThat(found.getDescription()).isEqualTo("자유로운 소통을 위한 커뮤니티입니다.");
    }

    @Test
    @DisplayName("게시판 저장 및 조회")
    void saveAndFind() {
        // when
        Board found = boardRepository.findById(board.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(board.getName());
    }

    // H2 데이터베이스에서는 cascade 삭제가 작동하지 않음
    // 따라서 H2 기반 환경에서는 테스트 코드에서 삭제 기능을 테스트하지 않음
    // 나중에 까먹지 말고 주석 해제하고 테스트 코드 작성해야 함
    // @Test
    // @DisplayName("게시판 삭제 - 연관된 게시글도 함께 삭제 (cascade)")
    // void deleteBoard() {
    //     // given
    //     assertThat(postRepository.findById(post.getId())).isPresent();
    //     assertThat(postLikeRepository.findById(postLike.getId())).isPresent();
    //     assertThat(postCommentRepository.findById(postComment.getId())).isPresent();
    //     assertThat(boardRepository.findById(board.getId())).isPresent();

    //     // when - Board만 삭제하면 연관된 모든 엔티티가 자동 삭제됨 (cascade)
    //     boardRepository.delete(board);

    //     // then
    //     assertThat(boardRepository.findByName("커뮤니티")).isNull();
    //     assertThat(postRepository.findById(post.getId())).isEmpty();
    //     assertThat(postLikeRepository.findById(postLike.getId())).isEmpty();
    //     assertThat(postCommentRepository.findById(postComment.getId())).isEmpty();
    // }
} 