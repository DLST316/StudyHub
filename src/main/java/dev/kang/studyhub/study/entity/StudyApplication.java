package dev.kang.studyhub.study.entity;

import dev.kang.studyhub.study.model.ApplicationStatus;
import dev.kang.studyhub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스터디 신청 엔티티
 * 
 * 사용자가 스터디에 신청한 정보를 담는 엔티티입니다.
 * User와 Study 엔티티와 각각 N:1 관계를 가집니다.
 * 한 사용자가 한 스터디에 중복 신청할 수 없도록 제약조건이 있습니다.
 */
@Entity
@Table(name = "study_applications", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "study_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 신청한 사용자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 신청한 스터디
     * Study 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    /**
     * 신청 상태
     * PENDING: 대기중, APPROVED: 승인됨, REJECTED: 거절됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /**
     * 신청일
     */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    /**
     * 신청 시 자동으로 현재 시간과 PENDING 상태 설정
     */
    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }

    /**
     * StudyApplication 엔티티 생성 빌더
     */
    @Builder
    public StudyApplication(User user, Study study, ApplicationStatus status) {
        this.user = user;
        this.study = study;
        this.status = status != null ? status : ApplicationStatus.PENDING;
    }

    /**
     * 신청 상태를 승인으로 변경
     */
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    /**
     * 신청 상태를 거절로 변경
     */
    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }

    /**
     * 신청 상태가 대기중인지 확인
     */
    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    /**
     * 신청 상태가 승인된지 확인
     */
    public boolean isApproved() {
        return this.status == ApplicationStatus.APPROVED;
    }

    /**
     * 신청 상태가 거절된지 확인
     */
    public boolean isRejected() {
        return this.status == ApplicationStatus.REJECTED;
    }
} 