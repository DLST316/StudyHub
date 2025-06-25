package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.ReportRepository;
import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.board.BoardRepository;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminDashboardController 단위 테스트
 *
 * - 대시보드 통계 API
 * - 최근 신고 내역 API
 */
@WebMvcTest(AdminDashboardController.class)
class AdminDashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PostRepository postRepository;
    @MockBean
    private PostCommentRepository commentRepository;
    @MockBean
    private StudyRepository studyRepository;
    @MockBean
    private BoardRepository boardRepository;
    @MockBean
    private ReportRepository reportRepository;

    @Test
    @DisplayName("대시보드 통계 API - 정상 응답")
    void getDashboardStats_success() throws Exception {
        // given
        when(userRepository.count()).thenReturn(10L);
        when(postRepository.count()).thenReturn(20L);
        when(commentRepository.count()).thenReturn(30L);
        when(studyRepository.count()).thenReturn(5L);
        when(boardRepository.count()).thenReturn(1L);
        when(reportRepository.countByIsResolvedFalse()).thenReturn(2L);
        when(userRepository.countByIsBlockedTrue()).thenReturn(1L);

        // when & then
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalPosts").value(20))
                .andExpect(jsonPath("$.totalComments").value(30))
                .andExpect(jsonPath("$.totalStudies").value(5))
                .andExpect(jsonPath("$.totalBoards").value(1))
                .andExpect(jsonPath("$.pendingReports").value(2))
                .andExpect(jsonPath("$.blockedUsers").value(1));
    }

    @Test
    @DisplayName("최근 신고 내역 API - 정상 응답")
    void getRecentReports_success() throws Exception {
        // given
        when(reportRepository.findTop10ByOrderByReportedAtDesc()).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/admin/dashboard/recent-reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
} 