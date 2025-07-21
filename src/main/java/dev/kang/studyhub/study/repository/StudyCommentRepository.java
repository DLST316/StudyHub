package dev.kang.studyhub.study.repository;

import dev.kang.studyhub.study.entity.Study;
import dev.kang.studyhub.study.entity.StudyComment;
import dev.kang.studyhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 스터디 댓글 리포지토리
 * 
 * 스터디 댓글의 데이터베이스 접근을 담당합니다.
 * 스터디별 댓글 조회, 사용자별 댓글 조회 등의 기능을 제공합니다.
 */
public interface StudyCommentRepository extends JpaRepository<StudyComment, Long> {

    /**
     * 특정 스터디의 모든 댓글을 작성일 순으로 조회
     */
    @Query("SELECT c FROM StudyComment c WHERE c.study = :study ORDER BY c.createdAt ASC")
    List<StudyComment> findByStudyOrderByCreatedAtAsc(@Param("study") Study study);

    /**
     * 특정 스터디의 댓글을 페이징하여 조회 (최신순)
     */
    @Query("SELECT c FROM StudyComment c WHERE c.study = :study ORDER BY c.createdAt DESC")
    Page<StudyComment> findByStudyOrderByCreatedAtDesc(@Param("study") Study study, Pageable pageable);

    /**
     * 특정 사용자가 작성한 모든 댓글 조회
     */
    @Query("SELECT c FROM StudyComment c WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<StudyComment> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    /**
     * 특정 스터디에서 특정 사용자가 작성한 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM StudyComment c WHERE c.study = :study AND c.user = :user")
    long countByStudyAndUser(@Param("study") Study study, @Param("user") User user);

    /**
     * 특정 스터디의 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM StudyComment c WHERE c.study = :study")
    long countByStudy(@Param("study") Study study);
} 