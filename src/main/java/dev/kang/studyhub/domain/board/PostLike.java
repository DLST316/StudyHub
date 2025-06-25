package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 게시글 추천/비추천 엔티티
 * 사용자별로 게시글에 대한 추천/비추천 상태를 관리합니다.
 * 한 사용자는 한 게시글에 하나의 추천/비추천만 할 수 있습니다.
 */
@Entity
@Table(name = "post_likes", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"post_id", "user_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_postlike_post", foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE"))
    private Post post;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 추천/비추천 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeType type;

    /** 생성일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 생성 시 자동으로 현재 시간 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 추천/비추천 타입을 정의하는 enum
     */
    public enum LikeType {
        LIKE("추천"),
        DISLIKE("비추천");

        private final String displayName;

        LikeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 