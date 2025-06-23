package dev.kang.studyhub.domain.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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

    // getter/setter 생략 (롬복 사용 가능)
} 