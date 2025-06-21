package dev.kang.studyhub.domain.study.entity;

import dev.kang.studyhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 스터디 엔티티
 * 
 * 스터디의 기본 정보를 담는 엔티티입니다.
 * 스터디 개설자(leader)와 1:N 관계를 가지며,
 * 스터디 신청(StudyApplication)과 1:N 관계를 가집니다.
 */
@Entity
@Table(name = "studies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 스터디 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 스터디 소개글
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 스터디 개설자 (스터디장)
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    /**
     * 모집 인원 제한
     */
    @Column(name = "recruitment_limit")
    private Integer recruitmentLimit;

    /**
     * 모집 조건 (전공, 학교, 기타 조건 등)
     */
    @Column(columnDefinition = "TEXT")
    private String requirement;

    /**
     * 모집 마감일
     */
    private LocalDate deadline;

    /**
     * 스터디 생성일
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 스터디 생성 시 자동으로 현재 시간 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 스터디 엔티티 생성 빌더
     */
    @Builder
    public Study(String title, String description, User leader, 
                Integer recruitmentLimit, String requirement, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.leader = leader;
        this.recruitmentLimit = recruitmentLimit;
        this.requirement = requirement;
        this.deadline = deadline;
    }

    /**
     * 스터디 정보 수정 메서드
     */
    public void updateStudy(String title, String description, 
                          Integer recruitmentLimit, String requirement, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.recruitmentLimit = recruitmentLimit;
        this.requirement = requirement;
        this.deadline = deadline;
    }

    /**
     * 스터디 개설자인지 확인하는 메서드
     */
    public boolean isLeader(User user) {
        return this.leader.getId().equals(user.getId());
    }

    /**
     * 모집 마감일이 지났는지 확인하는 메서드
     */
    public boolean isDeadlinePassed() {
        return deadline != null && LocalDate.now().isAfter(deadline);
    }
} 