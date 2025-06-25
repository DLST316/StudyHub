package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * BoardService 단위 테스트
 *
 * - 커뮤니티 게시판 조회
 * - 게시판이 존재하지 않는 경우 처리
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    @DisplayName("커뮤니티 게시판 조회 - 성공")
    void getCommunityBoard_success() throws Exception {
        // given
        Board communityBoard = new Board();
        communityBoard.setName("커뮤니티");
        communityBoard.setDescription("자유로운 소통을 위한 커뮤니티입니다.");
        communityBoard.setCreatedAt(LocalDateTime.now());
        boardRepository.save(communityBoard);

        when(boardRepository.findByName("커뮤니티")).thenReturn(communityBoard);

        // when
        Board result = boardService.getCommunityBoard();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("커뮤니티");
        assertThat(result.getDescription()).isEqualTo("자유로운 소통을 위한 커뮤니티입니다.");
    }

    @Test
    @DisplayName("커뮤니티 게시판 조회 - null 반환")
    void getCommunityBoard_returnsNull() throws Exception {
        // given
        when(boardRepository.findByName("커뮤니티")).thenReturn(null);

        // when
        Board result = boardService.getCommunityBoard();

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("커뮤니티 게시판 조회 - 빈 게시판 정보")
    void getCommunityBoard_emptyBoard() throws Exception {
        // given
        Board emptyBoard = new Board();
        emptyBoard.setId(1L);
        emptyBoard.setName("커뮤니티");
        emptyBoard.setDescription(null);
        emptyBoard.setCreatedAt(LocalDateTime.now());

        when(boardRepository.findByName("커뮤니티")).thenReturn(emptyBoard);

        // when
        Board result = boardService.getCommunityBoard();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("커뮤니티");
        assertThat(result.getDescription()).isNull();
    }
} 