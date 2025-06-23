package dev.kang.studyhub.service.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyComment;
import dev.kang.studyhub.domain.study.repository.StudyCommentRepository;
import dev.kang.studyhub.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudyCommentService 클래스의 단위 테스트
 *
 * 테스트 대상:
 * - 댓글 작성/수정/삭제/조회
 * - 권한 및 예외 상황
 */
class StudyCommentServiceTest {

    @Mock
    private StudyCommentRepository studyCommentRepository;
    @Mock
    private StudyApplicationService studyApplicationService;
    @InjectMocks
    private StudyCommentService studyCommentService;

    private User user;
    private User leader;
    private Study study;
    private StudyComment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().name("댓글작성자").build();
        user.setId(1L);
        leader = User.builder().name("스터디장").build();
        leader.setId(2L);
        study = Study.builder().title("테스트 스터디").leader(leader).build();
        // Study 엔티티에 setId가 없으면 reflection 등으로 주입 필요
        try {
            java.lang.reflect.Field idField = Study.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(study, 10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        comment = StudyComment.builder().content("댓글 내용").user(user).study(study).build();
    }

    @Test
    @DisplayName("특정 스터디의 모든 댓글을 조회할 수 있어야 한다")
    void getCommentsByStudy_Success() {
        when(studyCommentRepository.findByStudyOrderByCreatedAtAsc(study)).thenReturn(List.of(comment));
        List<StudyComment> result = studyCommentService.getCommentsByStudy(study);
        assertThat(result).hasSize(1);
        verify(studyCommentRepository).findByStudyOrderByCreatedAtAsc(study);
    }

    @Test
    @DisplayName("특정 스터디의 댓글을 페이징하여 조회할 수 있어야 한다")
    void getCommentsByStudyWithPaging_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StudyComment> page = new PageImpl<>(List.of(comment));
        when(studyCommentRepository.findByStudyOrderByCreatedAtDesc(study, pageable)).thenReturn(page);
        Page<StudyComment> result = studyCommentService.getCommentsByStudyWithPaging(study, pageable);
        assertThat(result.getContent()).hasSize(1);
        verify(studyCommentRepository).findByStudyOrderByCreatedAtDesc(study, pageable);
    }

    @Test
    @DisplayName("특정 사용자가 작성한 모든 댓글을 조회할 수 있어야 한다")
    void getCommentsByUser_Success() {
        when(studyCommentRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(comment));
        List<StudyComment> result = studyCommentService.getCommentsByUser(user);
        assertThat(result).hasSize(1);
        verify(studyCommentRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    @DisplayName("스터디장 또는 승인된 사용자는 댓글을 작성할 수 있다")
    void createComment_Success() {
        // 저장 요청한 객체를 그대로 반환
        when(studyCommentRepository.save(any(StudyComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // 스터디장
        StudyComment result = studyCommentService.createComment("내용", leader, study);
        assertThat(result.getContent()).isEqualTo("내용");
        // 승인된 멤버
        when(studyApplicationService.isApprovedMember(user, study)).thenReturn(true);
        StudyComment result2 = studyCommentService.createComment("내용2", user, study);
        assertThat(result2.getContent()).isEqualTo("내용2");
    }

    @Test
    @DisplayName("승인되지 않은 사용자는 댓글 작성 시 예외가 발생한다")
    void createComment_NotApproved_ThrowsException() {
        when(studyApplicationService.isApprovedMember(user, study)).thenReturn(false);
        assertThatThrownBy(() -> studyCommentService.createComment("내용", user, study))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("스터디에 승인된 사용자만");
    }

    @Test
    @DisplayName("댓글 내용이 비어있으면 예외가 발생한다")
    void createComment_EmptyContent_ThrowsException() {
        assertThatThrownBy(() -> studyCommentService.createComment(" ", user, study))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("댓글 작성자가 아니면 댓글 수정 시 예외가 발생한다")
    void updateComment_NotAuthor_ThrowsException() {
        User other = User.builder().id(99L).build();
        when(studyCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> studyCommentService.updateComment(1L, "수정", other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("댓글 작성자만");
    }

    @Test
    @DisplayName("댓글 작성자는 댓글을 수정할 수 있다")
    void updateComment_Success() {
        when(studyCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        StudyComment updated = studyCommentService.updateComment(1L, "수정된 내용", user);
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글 작성자 또는 스터디장만 댓글을 삭제할 수 있다")
    void deleteComment_Success() {
        // 댓글 작성자
        when(studyCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        studyCommentService.deleteComment(1L, user);
        verify(studyCommentRepository).delete(comment);
        // 스터디장
        when(studyCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        studyCommentService.deleteComment(1L, leader);
        verify(studyCommentRepository, times(2)).delete(comment);
    }

    @Test
    @DisplayName("댓글 작성자/스터디장이 아니면 댓글 삭제 시 예외가 발생한다")
    void deleteComment_NotAllowed_ThrowsException() {
        User stranger = User.builder().id(99L).build();
        when(studyCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> studyCommentService.deleteComment(1L, stranger))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("댓글 작성자 또는 스터디 개설자만");
    }

    @Test
    @DisplayName("특정 스터디의 댓글 수를 조회할 수 있다")
    void getCommentCountByStudy_Success() {
        when(studyCommentRepository.countByStudy(study)).thenReturn(3L);
        long count = studyCommentService.getCommentCountByStudy(study);
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에서 작성한 댓글 수를 조회할 수 있다")
    void getCommentCountByStudyAndUser_Success() {
        when(studyCommentRepository.countByStudyAndUser(study, user)).thenReturn(2L);
        long count = studyCommentService.getCommentCountByStudyAndUser(study, user);
        assertThat(count).isEqualTo(2L);
    }
} 