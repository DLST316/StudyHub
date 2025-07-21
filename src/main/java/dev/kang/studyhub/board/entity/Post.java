package dev.kang.studyhub.board.entity;

import dev.kang.studyhub.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_board", foreignKeyDefinition = "FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE"))
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

    /** 추천/비추천 목록 (양방향 매핑) */
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    /** 댓글 목록 (양방향 매핑) */
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostComment> postComments = new ArrayList<>();

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