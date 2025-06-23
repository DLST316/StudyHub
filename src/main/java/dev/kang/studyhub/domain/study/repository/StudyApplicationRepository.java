package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * StudyApplication 엔티티의 데이터 접근을 위한 리포지토리
 * 
 * 스터디 신청 관련 데이터베이스 작업을 담당합니다.
 * - 스터디 신청 CRUD 작업
 * - 신청 상태별 조회
 * - 사용자별/스터디별 신청 조회
 */
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    /**
     * 특정 사용자가 특정 스터디에 신청한 내역 조회
     */
    Optional<StudyApplication> findByUserAndStudy(User user, Study study);

    /**
     * 특정 사용자가 신청한 모든 스터디 신청 내역 조회
     */
    List<StudyApplication> findByUserOrderByAppliedAtDesc(User user);

    /**
     * 특정 스터디에 신청한 모든 신청 내역 조회
     */
    List<StudyApplication> findByStudyOrderByAppliedAtDesc(Study study);

    /**
     * 특정 스터디의 특정 상태 신청 내역 조회
     */
    List<StudyApplication> findByStudyAndStatusOrderByAppliedAtDesc(Study study, ApplicationStatus status);

    /**
     * 특정 사용자의 특정 상태 신청 내역 조회
     */
    List<StudyApplication> findByUserAndStatusOrderByAppliedAtDesc(User user, ApplicationStatus status);

    /**
     * 특정 스터디의 승인된 신청 개수 조회
     */
    long countByStudyAndStatus(Study study, ApplicationStatus status);

    /**
     * 특정 사용자가 특정 스터디에 이미 신청했는지 확인
     */
    boolean existsByUserAndStudy(User user, Study study);

    /**
     * 특정 사용자가 특정 스터디에 승인된 신청이 있는지 확인
     */
    boolean existsByUserAndStudyAndStatus(User user, Study study, ApplicationStatus status);

    /**
     * 특정 스터디의 모든 신청 개수 조회
     */
    long countByStudy(Study study);

    /**
     * 특정 사용자가 신청한 스터디 개수 조회
     */
    long countByUser(User user);

    /**
     * 특정 사용자가 승인된 스터디 신청 개수 조회
     */
    long countByUserAndStatus(User user, ApplicationStatus status);

    /**
     * 특정 스터디의 대기중인 신청 개수 조회
     */
    @Query("SELECT COUNT(sa) FROM StudyApplication sa WHERE sa.study = :study AND sa.status = 'PENDING'")
    long countPendingApplicationsByStudy(@Param("study") Study study);

    /**
     * 특정 스터디의 승인된 신청 개수 조회
     */
    @Query("SELECT COUNT(sa) FROM StudyApplication sa WHERE sa.study = :study AND sa.status = 'APPROVED'")
    long countApprovedApplicationsByStudy(@Param("study") Study study);

    @Query("select sa from StudyApplication sa " +
            "join fetch sa.study s " +
            "join fetch s.leader " +
            "where sa.user = :user " +
            "order by sa.appliedAt desc")
    List<StudyApplication> findByUserWithStudyAndLeader(@Param("user") User user);

    List<StudyApplication> findByUser(User user);

    List<StudyApplication> findByStudy(Study study);

    List<StudyApplication> findByUserAndStatus(User user, ApplicationStatus status);
} 