package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.admin.AdminDashboardController;
import dev.kang.studyhub.board.repository.BoardRepository;
import dev.kang.studyhub.board.repository.PostCommentRepository;
import dev.kang.studyhub.board.repository.PostRepository;
import dev.kang.studyhub.board.repository.ReportRepository;
import dev.kang.studyhub.study.repository.StudyRepository;
import dev.kang.studyhub.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 관리자 대시보드 컨트롤러 단위 테스트
 * 
 * 이 컨트롤러는 단순한 통계 조회만 수행하므로 단위 테스트로 충분합니다.
 * Spring Security나 복잡한 비즈니스 로직이 없어 Mock을 사용한 테스트가 적합합니다.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PostRepository postRepository;
    
    @Mock
    private PostCommentRepository commentRepository;
    
    @Mock
    private StudyRepository studyRepository;
    
    @Mock
    private BoardRepository boardRepository;
    
    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private AdminDashboardController controller;

    @Test
    @DisplayName("대시보드 통계 데이터 조회")
    void getDashboardStats() {
        // given
        given(userRepository.count()).willReturn(100L);
        given(postRepository.count()).willReturn(500L);
        given(commentRepository.count()).willReturn(2000L);
        given(studyRepository.count()).willReturn(50L);
        given(boardRepository.count()).willReturn(3L);
        given(reportRepository.countByIsResolvedFalse()).willReturn(10L);
        given(userRepository.countByIsBlockedTrue()).willReturn(5L);

        // when
        ResponseEntity<Map<String, Object>> response = controller.getDashboardStats();
        
        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> stats = response.getBody();
        
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalUsers")).isEqualTo(100L);
        assertThat(stats.get("totalPosts")).isEqualTo(500L);
        assertThat(stats.get("totalComments")).isEqualTo(2000L);
        assertThat(stats.get("totalStudies")).isEqualTo(50L);
        assertThat(stats.get("totalBoards")).isEqualTo(3L);
        assertThat(stats.get("pendingReports")).isEqualTo(10L);
        assertThat(stats.get("blockedUsers")).isEqualTo(5L);
    }

    @Test
    @DisplayName("최근 신고 내역 조회")
    void getRecentReports() {
        // when
        ResponseEntity<?> response = controller.getRecentReports();
        
        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
} 