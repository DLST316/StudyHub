package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 신고 엔티티
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 신고자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;
    
    /**
     * 신고 대상 타입 (POST, COMMENT, USER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;
    
    /**
     * 신고 대상 ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    /**
     * 신고 사유
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;
    
    /**
     * 신고 상세 내용
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;
    
    /**
     * 신고 일시
     */
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();
    
    /**
     * 처리 일시
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    /**
     * 처리자 (관리자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;
    
    /**
     * 처리 결과 메모
     */
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;
    
    /**
     * 신고 대상 타입 열거형
     */
    public enum ReportTargetType {
        POST, COMMENT, USER
    }
    
    /**
     * 신고 사유 열거형
     */
    public enum ReportReason {
        SPAM("스팸"),
        INAPPROPRIATE_CONTENT("부적절한 내용"),
        HARASSMENT("괴롭힘"),
        COPYRIGHT_VIOLATION("저작권 침해"),
        OTHER("기타");
        
        private final String description;
        
        ReportReason(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 신고 상태 열거형
     */
    public enum ReportStatus {
        PENDING("대기중"),
        APPROVED("승인"),
        REJECTED("거부"),
        RESOLVED("처리완료");
        
        private final String description;
        
        ReportStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 신고 생성
     */
    public static Report createReport(User reporter, ReportTargetType targetType, Long targetId, 
                                    ReportReason reason, String description) {
        Report report = new Report();
        report.reporter = reporter;
        report.targetType = targetType;
        report.targetId = targetId;
        report.reason = reason;
        report.description = description;
        return report;
    }
    
    /**
     * 신고 처리
     */
    public void resolve(ReportStatus status, User resolver, String resolutionNote) {
        this.status = status;
        this.resolver = resolver;
        this.resolutionNote = resolutionNote;
        this.resolvedAt = LocalDateTime.now();
    }
    
    /**
     * 처리 완료 여부
     */
    public boolean isResolved() {
        return status != ReportStatus.PENDING;
    }
} 