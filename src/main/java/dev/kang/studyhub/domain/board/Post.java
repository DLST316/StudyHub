package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * 게시글(Post) 엔티티
 * posts 테이블과 매핑
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시판(커뮤니티) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 제목 */
    @Column(nullable = false)
    private String title;

    /** 본문 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    /** 추천수 */
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /** 비추천수 */
    @Column(name = "dislike_count", nullable = false)
    private int dislikeCount = 0;

    /** 공지글 여부 */
    @Column(name = "is_notice", nullable = false)
    private boolean isNotice = false;

    /** 생성일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 생성/수정일시 자동 세팅
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getter/setter 생략 (롬복 사용 가능)
} 