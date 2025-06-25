package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyComment;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyCommentRepository 통합 테스트
 *
 * - 스터디 댓글 CRUD 작업
 * - 스터디별 댓글 조회
 * - 사용자별 댓글 조회
 * - 페이징 처리
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StudyCommentRepositoryTest {
    @Autowired
    private StudyCommentRepository studyCommentRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User leader;
    private Study study1;
    private Study study2;
    private StudyComment comment1;
    private StudyComment comment2;
    private StudyComment comment3;
    private StudyComment comment4;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        user1 = User.builder()
                .name("댓글작성자1")
                .email("commenter1@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .name("댓글작성자2")
                .email("commenter2@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user2);

        leader = User.builder()
                .name("스터디장")
                .email("leader@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(leader);

        // 테스트용 스터디 생성
        study1 = Study.builder()
                .title("Java 스터디")
                .description("Java 기초부터 심화까지")
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();
        studyRepository.save(study1);

        study2 = Study.builder()
                .title("Spring Boot 스터디")
                .description("Spring Boot 실전 프로젝트")
                .leader(leader)
                .recruitmentLimit(3)
                .requirement("Java, Spring 기초")
                .deadline(LocalDate.now().plusDays(14))
                .build();
        studyRepository.save(study2);

        // 테스트용 댓글 생성
        comment1 = StudyComment.builder()
                .content("안녕하세요! Java 스터디에 참여하게 되어 기쁩니다.")
                .user(user1)
                .study(study1)
                .build();
        studyCommentRepository.save(comment1);

        comment2 = StudyComment.builder()
                .content("저도 Java를 배우고 싶어요. 함께 공부해요!")
                .user(user2)
                .study(study1)
                .build();
        studyCommentRepository.save(comment2);

        comment3 = StudyComment.builder()
                .content("Spring Boot 스터디도 참여하고 싶어요.")
                .user(user1)
                .study(study2)
                .build();
        studyCommentRepository.save(comment3);

        comment4 = StudyComment.builder()
                .content("스터디 진행 방식에 대해 궁금한 점이 있어요.")
                .user(user2)
                .study(study1)
                .build();
        studyCommentRepository.save(comment4);
    }

    @Test
    @DisplayName("특정 스터디의 모든 댓글을 작성일 순으로 조회")
    void findByStudyOrderByCreatedAtAsc() {
        // when
        List<StudyComment> comments = studyCommentRepository.findByStudyOrderByCreatedAtAsc(study1);

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments).allMatch(comment -> comment.getStudy().getId().equals(study1.getId()));
        assertThat(comments.get(0).getCreatedAt()).isBeforeOrEqualTo(comments.get(1).getCreatedAt());
        assertThat(comments.get(1).getCreatedAt()).isBeforeOrEqualTo(comments.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("특정 스터디의 댓글을 페이징하여 조회 (최신순)")
    void findByStudyOrderByCreatedAtDesc() {
        // when
        Page<StudyComment> page = studyCommentRepository.findByStudyOrderByCreatedAtDesc(study1, PageRequest.of(0, 2));

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(comment -> comment.getStudy().getId().equals(study1.getId()));
        assertThat(page.getContent().get(0).getCreatedAt()).isAfterOrEqualTo(page.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 사용자가 작성한 모든 댓글 조회")
    void findByUserOrderByCreatedAtDesc() {
        // when
        List<StudyComment> comments = studyCommentRepository.findByUserOrderByCreatedAtDesc(user1);

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(comment -> comment.getUser().getId().equals(user1.getId()));
        assertThat(comments.get(0).getCreatedAt()).isAfterOrEqualTo(comments.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 스터디에서 특정 사용자가 작성한 댓글 수 조회")
    void countByStudyAndUser() {
        // when
        long count1 = studyCommentRepository.countByStudyAndUser(study1, user1);
        long count2 = studyCommentRepository.countByStudyAndUser(study1, user2);
        long count3 = studyCommentRepository.countByStudyAndUser(study2, user1);

        // then
        assertThat(count1).isEqualTo(1);
        assertThat(count2).isEqualTo(2);
        assertThat(count3).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 스터디의 댓글 수 조회")
    void countByStudy() {
        // when
        long count1 = studyCommentRepository.countByStudy(study1);
        long count2 = studyCommentRepository.countByStudy(study2);

        // then
        assertThat(count1).isEqualTo(3);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("스터디 댓글 저장 및 조회")
    void saveAndFind() {
        // given
        StudyComment newComment = StudyComment.builder()
                .content("새로운 댓글입니다.")
                .user(user1)
                .study(study2)
                .build();

        // when
        StudyComment saved = studyCommentRepository.save(newComment);
        StudyComment found = studyCommentRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getContent()).isEqualTo("새로운 댓글입니다.");
        assertThat(found.getUser().getId()).isEqualTo(user1.getId());
        assertThat(found.getStudy().getId()).isEqualTo(study2.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("스터디 댓글 수정")
    void updateComment() {
        // given
        String newContent = "수정된 댓글 내용입니다.";
        assertThat(comment1.getContent()).isNotEqualTo(newContent);

        // when
        comment1.updateContent(newContent);
        studyCommentRepository.save(comment1);
        StudyComment updated = studyCommentRepository.findById(comment1.getId()).orElse(null);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getContent()).isEqualTo(newContent);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
    }

    @Test
    @DisplayName("스터디 댓글 삭제")
    void deleteComment() {
        // given
        assertThat(studyCommentRepository.findById(comment1.getId())).isPresent();

        // when
        studyCommentRepository.delete(comment1);

        // then
        assertThat(studyCommentRepository.findById(comment1.getId())).isEmpty();
        assertThat(studyCommentRepository.countByStudy(study1)).isEqualTo(2);
    }

    @Test
    @DisplayName("댓글 작성자 확인")
    void isAuthor() {
        // when & then
        assertThat(comment1.isAuthor(user1)).isTrue();
        assertThat(comment1.isAuthor(user2)).isFalse();
        assertThat(comment2.isAuthor(user2)).isTrue();
        assertThat(comment2.isAuthor(user1)).isFalse();
    }

    @Test
    @DisplayName("여러 스터디의 댓글 조회")
    void findCommentsFromMultipleStudies() {
        // when
        List<StudyComment> study1Comments = studyCommentRepository.findByStudyOrderByCreatedAtAsc(study1);
        List<StudyComment> study2Comments = studyCommentRepository.findByStudyOrderByCreatedAtAsc(study2);

        // then
        assertThat(study1Comments).hasSize(3);
        assertThat(study2Comments).hasSize(1);
        assertThat(study1Comments).allMatch(comment -> comment.getStudy().getId().equals(study1.getId()));
        assertThat(study2Comments).allMatch(comment -> comment.getStudy().getId().equals(study2.getId()));
    }

    @Test
    @DisplayName("사용자별 댓글 작성 현황")
    void userCommentStatistics() {
        // when
        List<StudyComment> user1Comments = studyCommentRepository.findByUserOrderByCreatedAtDesc(user1);
        List<StudyComment> user2Comments = studyCommentRepository.findByUserOrderByCreatedAtDesc(user2);

        // then
        assertThat(user1Comments).hasSize(2);
        assertThat(user2Comments).hasSize(2);
        assertThat(user1Comments).allMatch(comment -> comment.getUser().getId().equals(user1.getId()));
        assertThat(user2Comments).allMatch(comment -> comment.getUser().getId().equals(user2.getId()));
    }
} 