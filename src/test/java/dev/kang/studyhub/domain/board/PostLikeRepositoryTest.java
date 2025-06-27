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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostLikeRepository 통합 테스트
 *
 * - 특정 사용자가 특정 게시글에 한 추천/비추천 조회
 * - 특정 사용자가 특정 게시글에 추천/비추천을 했는지 확인
 * - 특정 게시글의 추천 개수 조회
 * - 특정 게시글의 비추천 개수 조회
 * - 특정 사용자가 특정 게시글에 한 추천/비추천 삭제
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PostLikeRepositoryTest {
    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    private User user1;
    private User user2;
    private Board board;
    private Post post1;
    private Post post2;
    private PostLike like1;
    private PostLike like2;
    private PostLike dislike1;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리 (중복/무결성 에러 방지)
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 생성
        user1 = User.builder()
                .name("사용자1")
                .email("user1@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .name("사용자2")
                .email("user2@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user2);

        board = new Board();
        // board name을 매번 다르게 생성하여 중복 방지
        board.setName("커뮤니티_" + UUID.randomUUID());
        board.setDescription("자유로운 소통을 위한 커뮤니티입니다.");
        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);

        post1 = new Post();
        post1.setTitle("테스트 게시글 1");
        post1.setContent("테스트 내용 1");
        post1.setUser(user1);
        post1.setBoard(board);
        post1.setCreatedAt(LocalDateTime.now());
        post1.setViewCount(0);
        post1.setLikeCount(0);
        post1.setDislikeCount(0);
        postRepository.save(post1);

        post2 = new Post();
        post2.setTitle("테스트 게시글 2");
        post2.setContent("테스트 내용 2");
        post2.setUser(user2);
        post2.setBoard(board);
        post2.setCreatedAt(LocalDateTime.now());
        post2.setViewCount(0);
        post2.setLikeCount(0);
        post2.setDislikeCount(0);
        postRepository.save(post2);

        // 좋아요/싫어요 데이터 생성
        like1 = new PostLike();
        like1.setPost(post1);
        like1.setUser(user1);
        like1.setType(PostLike.LikeType.LIKE);
        like1.setCreatedAt(LocalDateTime.now());
        postLikeRepository.save(like1);

        like2 = new PostLike();
        like2.setPost(post1);
        like2.setUser(user2);
        like2.setType(PostLike.LikeType.LIKE);
        like2.setCreatedAt(LocalDateTime.now());
        postLikeRepository.save(like2);

        dislike1 = new PostLike();
        dislike1.setPost(post2);
        dislike1.setUser(user1);
        dislike1.setType(PostLike.LikeType.DISLIKE);
        dislike1.setCreatedAt(LocalDateTime.now());
        postLikeRepository.save(dislike1);
    }

    @Test
    @DisplayName("특정 사용자가 특정 게시글에 한 추천/비추천 조회 - 성공")
    void findByPostAndUser_success() throws Exception {
        // when
        Optional<PostLike> result = postLikeRepository.findByPostAndUser(post1, user1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPost().getId()).isEqualTo(post1.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user1.getId());
        assertThat(result.get().getType()).isEqualTo(PostLike.LikeType.LIKE);
    }

    @Test
    @DisplayName("특정 사용자가 특정 게시글에 한 추천/비추천 조회 - 존재하지 않는 경우")
    void findByPostAndUser_notFound() throws Exception {
        // when
        Optional<PostLike> result = postLikeRepository.findByPostAndUser(post2, user2);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특정 사용자가 특정 게시글에 추천/비추천을 했는지 확인 - 존재하는 경우")
    void existsByPostAndUser_exists() throws Exception {
        // when
        boolean exists = postLikeRepository.existsByPostAndUser(post1, user1);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 사용자가 특정 게시글에 추천/비추천을 했는지 확인 - 존재하지 않는 경우")
    void existsByPostAndUser_notExists() throws Exception {
        // when
        boolean exists = postLikeRepository.existsByPostAndUser(post2, user2);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 게시글의 추천 개수 조회")
    void countLikesByPost() throws Exception {
        // when
        long likeCount = postLikeRepository.countLikesByPost(post1);

        // then
        assertThat(likeCount).isEqualTo(2); // user1, user2가 post1에 좋아요
    }

    @Test
    @DisplayName("특정 게시글의 비추천 개수 조회")
    void countDislikesByPost() throws Exception {
        // when
        long dislikeCount = postLikeRepository.countDislikesByPost(post2);

        // then
        assertThat(dislikeCount).isEqualTo(1); // user1이 post2에 싫어요
    }

    @Test
    @DisplayName("특정 게시글의 추천 개수 조회 - 추천이 없는 경우")
    void countLikesByPost_noLikes() throws Exception {
        // when
        long likeCount = postLikeRepository.countLikesByPost(post2);

        // then
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 게시글의 비추천 개수 조회 - 비추천이 없는 경우")
    void countDislikesByPost_noDislikes() throws Exception {
        // when
        long dislikeCount = postLikeRepository.countDislikesByPost(post1);

        // then
        assertThat(dislikeCount).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 사용자가 특정 게시글에 한 추천/비추천 삭제")
    void deleteByPostAndUser() throws Exception {
        // given
        assertThat(postLikeRepository.existsByPostAndUser(post1, user1)).isTrue();

        // when
        postLikeRepository.deleteByPostAndUser(post1, user1);

        // then
        assertThat(postLikeRepository.existsByPostAndUser(post1, user1)).isFalse();
        assertThat(postLikeRepository.findByPostAndUser(post1, user1)).isEmpty();
    }

    @Test
    @DisplayName("좋아요/싫어요 타입별 조회")
    void findByType() throws Exception {
        // when
        long totalLikes = postLikeRepository.countLikesByPost(post1);
        long totalDislikes = postLikeRepository.countDislikesByPost(post2);

        // then
        assertThat(totalLikes).isEqualTo(2);
        assertThat(totalDislikes).isEqualTo(1);
    }
} 