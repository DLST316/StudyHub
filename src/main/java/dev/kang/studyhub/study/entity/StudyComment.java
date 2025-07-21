package dev.kang.studyhub.study.entity;

import dev.kang.studyhub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스터디 댓글 엔티티
 * 
 * 스터디 내에서 스터디원들이 대화를 나눌 수 있는 댓글 기능을 제공합니다.
 * 스터디에 승인된 사용자만 댓글을 작성할 수 있습니다.
 * User와 Study 엔티티와 각각 N:1 관계를 가집니다.
 */
@Entity
@Table(name = "study_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 댓글 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 댓글 작성자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 댓글이 속한 스터디
     * Study 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    /**
     * 댓글 작성일
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 댓글 수정일
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 댓글 작성 시 자동으로 현재 시간 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 댓글 수정 시 자동으로 수정 시간 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * StudyComment 엔티티 생성 빌더
     */
    @Builder
    public StudyComment(String content, User user, Study study) {
        this.content = content;
        this.user = user;
        this.study = study;
    }

    /**
     * 댓글 내용 수정 메서드
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 댓글 작성자인지 확인하는 메서드
     */
    public boolean isAuthor(User user) {
        return this.user.getId().equals(user.getId());
    }
} 