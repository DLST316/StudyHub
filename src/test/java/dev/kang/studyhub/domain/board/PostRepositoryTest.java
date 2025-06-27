package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostRepository 통합 테스트
 *
 * - 게시판 ID로 게시글 목록 조회
 * - 게시글 저장/삭제/조회
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    private User user;
    private Board board;
    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리 (중복/무결성 에러 방지)
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        user = User.builder()
                .name("테스트 사용자")
                .username("post_test_user")
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

        post1 = new Post();
        post1.setTitle("첫 번째 게시글");
        post1.setContent("첫 번째 내용");
        post1.setUser(user);
        post1.setBoard(board);
        post1.setCreatedAt(LocalDateTime.now());
        post1.setViewCount(0);
        post1.setLikeCount(0);
        post1.setDislikeCount(0);
        postRepository.save(post1);

        post2 = new Post();
        post2.setTitle("두 번째 게시글");
        post2.setContent("두 번째 내용");
        post2.setUser(user);
        post2.setBoard(board);
        post2.setCreatedAt(LocalDateTime.now());
        post2.setViewCount(0);
        post2.setLikeCount(0);
        post2.setDislikeCount(0);
        postRepository.save(post2);
    }

    @Test
    @DisplayName("게시판 ID로 게시글 목록 조회")
    void findByBoardId() {
        // when
        Page<Post> posts = postRepository.findByBoardId(board.getId(), PageRequest.of(0, 10));

        // then
        assertThat(posts.getContent()).hasSize(2);
        assertThat(posts.getContent()).extracting(Post::getTitle)
                .containsExactlyInAnyOrder("첫 번째 게시글", "두 번째 게시글");
    }

    @Test
    @DisplayName("게시글 저장 및 조회")
    void saveAndFind() {
        // when
        Post found = postRepository.findById(post1.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("첫 번째 게시글");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        // when
        postRepository.delete(post1);
        Page<Post> posts = postRepository.findByBoardId(board.getId(), PageRequest.of(0, 10));

        // then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("두 번째 게시글");
    }
} 