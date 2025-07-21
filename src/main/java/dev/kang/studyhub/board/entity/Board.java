package dev.kang.studyhub.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 커뮤니티(게시판) 엔티티
 * boards 테이블과 매핑, 단일 row("커뮤니티")만 사용
 */
@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시판명(예: 커뮤니티) */
    @Column(nullable = false, unique = true)
    private String name;

    /** 게시판 설명 */
    private String description;

    /** 생성일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 게시글 목록 (양방향 매핑) */
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    // getter/setter 생략 (롬복 사용 가능)
} 