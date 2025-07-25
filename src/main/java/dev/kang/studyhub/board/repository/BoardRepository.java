package dev.kang.studyhub.board.repository;

import dev.kang.studyhub.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 커뮤니티(게시판) JPA 리포지토리
 */
public interface BoardRepository extends JpaRepository<Board, Long> {
    Board findByName(String name);
} 