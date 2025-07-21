package dev.kang.studyhub.board.repository;

import dev.kang.studyhub.board.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 신고 Repository
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    /**
     * 미처리 신고 수 조회
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'PENDING'")
    long countByIsResolvedFalse();
    
    /**
     * 최근 신고 내역 조회 (최신순)
     */
    @Query("SELECT r FROM Report r ORDER BY r.reportedAt DESC")
    List<Report> findTop10ByOrderByReportedAtDesc();
    
    /**
     * 상태별 신고 목록 조회
     */
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    
    /**
     * 신고 대상 타입별 조회
     */
    Page<Report> findByTargetType(Report.ReportTargetType targetType, Pageable pageable);
    
    /**
     * 신고자별 신고 내역 조회
     */
    Page<Report> findByReporterId(Long reporterId, Pageable pageable);
    
    /**
     * 특정 대상에 대한 신고 내역 조회
     */
    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType AND r.targetId = :targetId")
    List<Report> findByTarget(@Param("targetType") Report.ReportTargetType targetType, 
                             @Param("targetId") Long targetId);
    
    /**
     * 처리자별 처리 내역 조회
     */
    Page<Report> findByResolverId(Long resolverId, Pageable pageable);
} 