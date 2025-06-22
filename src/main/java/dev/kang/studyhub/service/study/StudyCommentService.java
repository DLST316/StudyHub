package dev.kang.studyhub.service.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyComment;
import dev.kang.studyhub.domain.study.repository.StudyCommentRepository;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 스터디 댓글 서비스
 * 
 * 스터디 댓글의 비즈니스 로직을 처리합니다.
 * 댓글 작성, 수정, 삭제, 조회 등의 기능을 제공하며,
 * 스터디에 승인된 사용자만 댓글을 작성할 수 있도록 제한합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyCommentService {

    private final StudyCommentRepository studyCommentRepository;
    private final StudyApplicationService studyApplicationService;

    /**
     * 특정 스터디의 모든 댓글을 작성일 순으로 조회
     */
    public List<StudyComment> getCommentsByStudy(Study study) {
        return studyCommentRepository.findByStudyOrderByCreatedAtAsc(study);
    }

    /**
     * 특정 스터디의 댓글을 페이징하여 조회
     */
    public Page<StudyComment> getCommentsByStudyWithPaging(Study study, Pageable pageable) {
        return studyCommentRepository.findByStudyOrderByCreatedAtDesc(study, pageable);
    }

    /**
     * 특정 사용자가 작성한 모든 댓글 조회
     */
    public List<StudyComment> getCommentsByUser(User user) {
        return studyCommentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 댓글 작성
     * 스터디에 승인된 사용자만 댓글을 작성할 수 있습니다.
     */
    @Transactional
    public StudyComment createComment(String content, User user, Study study) {
        // 입력값 검증
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        
        if (study == null) {
            throw new IllegalArgumentException("스터디 정보가 없습니다.");
        }
        
        // 스터디 개설자는 항상 댓글 작성 가능
        if (study.isLeader(user)) {
            return studyCommentRepository.save(
                StudyComment.builder()
                    .content(content.trim())
                    .user(user)
                    .study(study)
                    .build()
            );
        }

        // 스터디에 승인된 사용자인지 확인
        try {
            if (!studyApplicationService.isApprovedMember(user, study)) {
                throw new IllegalStateException("스터디에 승인된 사용자만 댓글을 작성할 수 있습니다.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("댓글 작성 권한을 확인할 수 없습니다: " + e.getMessage());
        }

        return studyCommentRepository.save(
            StudyComment.builder()
                .content(content.trim())
                .user(user)
                .study(study)
                .build()
        );
    }

    /**
     * 댓글 수정
     * 댓글 작성자만 수정할 수 있습니다.
     */
    @Transactional
    public StudyComment updateComment(Long commentId, String content, User user) {
        StudyComment comment = studyCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(user)) {
            throw new IllegalStateException("댓글 작성자만 수정할 수 있습니다.");
        }

        comment.updateContent(content);
        return comment;
    }

    /**
     * 댓글 삭제
     * 댓글 작성자 또는 스터디 개설자만 삭제할 수 있습니다.
     */
    @Transactional
    public void deleteComment(Long commentId, User user) {
        StudyComment comment = studyCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자이거나 스터디 개설자인 경우에만 삭제 가능
        if (!comment.isAuthor(user) && !comment.getStudy().isLeader(user)) {
            throw new IllegalStateException("댓글 작성자 또는 스터디 개설자만 삭제할 수 있습니다.");
        }

        studyCommentRepository.delete(comment);
    }

    /**
     * 특정 스터디의 댓글 수 조회
     */
    public long getCommentCountByStudy(Study study) {
        return studyCommentRepository.countByStudy(study);
    }

    /**
     * 특정 사용자가 특정 스터디에서 작성한 댓글 수 조회
     */
    public long getCommentCountByStudyAndUser(Study study, User user) {
        return studyCommentRepository.countByStudyAndUser(study, user);
    }
} 