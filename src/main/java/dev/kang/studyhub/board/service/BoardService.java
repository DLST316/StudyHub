package dev.kang.studyhub.board.service;

import dev.kang.studyhub.board.entity.Board;
import dev.kang.studyhub.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티(게시판) 서비스
 * 단일 커뮤니티 row만 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;

    /**
     * "커뮤니티" 게시판 row 조회
     */
    public Board getCommunityBoard() {
        return boardRepository.findByName("커뮤니티");
    }
} 