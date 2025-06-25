package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.Report;
import dev.kang.studyhub.domain.board.ReportRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.web.admin.dto.ReportDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminReportController 통합 테스트
 *
 * - 신고 목록 조회
 * - 신고 상세 조회
 * - 신고 처리(승인/거부)
 * - 신고 통계
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminReportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportRepository reportRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("신고 목록 조회 API - 상태별 조회")
    @WithMockUser(roles = "ADMIN")
    void getReportsApi_status() throws Exception {
        // given
        Report mockReport = mock(Report.class);
        Page<Report> page = new PageImpl<>(List.of(mockReport));
        when(reportRepository.findByStatus(any(), any(PageRequest.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/admin/reports/api").param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("신고 목록 조회 API - 타입별 조회")
    @WithMockUser(roles = "ADMIN")
    void getReportsApi_targetType() throws Exception {
        // given
        Report mockReport = mock(Report.class);
        Page<Report> page = new PageImpl<>(List.of(mockReport));
        when(reportRepository.findByTargetType(any(), any(PageRequest.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/admin/reports/api").param("targetType", "POST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("신고 목록 조회 API - 전체 조회")
    @WithMockUser(roles = "ADMIN")
    void getReportsApi_all() throws Exception {
        // given
        Report mockReport = mock(Report.class);
        Page<Report> page = new PageImpl<>(List.of(mockReport));
        when(reportRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/admin/reports/api"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("신고 상세 조회 API - 성공")
    @WithMockUser(roles = "ADMIN")
    void getReport_success() throws Exception {
        // given
        Report report = mock(Report.class);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        // when & then
        mockMvc.perform(get("/admin/reports/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("신고 상세 조회 API - 실패")
    @WithMockUser(roles = "ADMIN")
    void getReport_notFound() throws Exception {
        // given
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/admin/reports/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("신고 처리 API - 성공")
    @WithMockUser(roles = "ADMIN")
    void resolveReport_success() throws Exception {
        // given
        Report report = mock(Report.class);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // when & then
        mockMvc.perform(post("/admin/reports/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\",\"resolutionNote\":\"처리 완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("신고가 처리되었습니다."));
    }

    @Test
    @DisplayName("신고 처리 API - 신고 없음")
    @WithMockUser(roles = "ADMIN")
    void resolveReport_notFound() throws Exception {
        // given
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/admin/reports/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\",\"resolutionNote\":\"처리 완료\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("신고 통계 API - 정상 응답")
    @WithMockUser(roles = "ADMIN")
    void getReportStats_success() throws Exception {
        // given
        when(reportRepository.countByIsResolvedFalse()).thenReturn(3L);
        when(reportRepository.count()).thenReturn(10L);

        // when & then
        mockMvc.perform(get("/admin/reports/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(3))
                .andExpect(jsonPath("$.total").value(10));
    }
} 