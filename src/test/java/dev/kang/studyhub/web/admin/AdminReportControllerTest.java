package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.Report;
import dev.kang.studyhub.domain.board.ReportRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.web.admin.dto.ReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 관리자 신고 처리 컨트롤러 단위 테스트
 * 
 * 이 컨트롤러는 신고 조회와 처리만 담당하므로 단위 테스트로 충분합니다.
 * Spring Security나 복잡한 비즈니스 로직이 없어 Mock을 사용한 테스트가 적합합니다.
 */
@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminReportController controller;

    private User testUser;
    private Report testReport;
    private Page<Report> reportPage;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .name("테스트 사용자")
                .username("admin_report_test_user")
                .email("test@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();

        testReport = Report.createReport(
                testUser,
                Report.ReportTargetType.POST,
                1L,
                Report.ReportReason.INAPPROPRIATE_CONTENT,
                "테스트 신고 사유"
        );
        testReport.setId(1L);
        testReport.setStatus(Report.ReportStatus.PENDING);

        reportPage = new PageImpl<>(List.of(testReport), PageRequest.of(0, 20), 1);
    }

    @Test
    @DisplayName("신고 목록 조회 - 전체 신고")
    void getReportsApi_AllReports() {
        // given
        given(reportRepository.findAll(any(Pageable.class))).willReturn(reportPage);

        // when
        Page<ReportDto> result = controller.getReportsApi(PageRequest.of(0, 20), null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Report.ReportStatus.PENDING.name());
    }

    @Test
    @DisplayName("신고 목록 조회 - 상태별 필터링")
    void getReportsApi_ByStatus() {
        // given
        given(reportRepository.findByStatus(Report.ReportStatus.PENDING, PageRequest.of(0, 20)))
                .willReturn(reportPage);

        // when
        Page<ReportDto> result = controller.getReportsApi(PageRequest.of(0, 20), Report.ReportStatus.PENDING, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(reportRepository).findByStatus(Report.ReportStatus.PENDING, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("신고 상세 조회 - 존재하는 신고")
    void getReport_ExistingReport() {
        // given
        given(reportRepository.findById(1L)).willReturn(Optional.of(testReport));

        // when
        ResponseEntity<ReportDto> response = controller.getReport(1L);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("신고 상세 조회 - 존재하지 않는 신고")
    void getReport_NonExistingReport() {
        // given
        given(reportRepository.findById(999L)).willReturn(Optional.empty());

        // when
        ResponseEntity<ReportDto> response = controller.getReport(999L);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("신고 처리 - 승인")
    void resolveReport_Approve() {
        // given
        given(reportRepository.findById(1L)).willReturn(Optional.of(testReport));
        given(userRepository.findAll()).willReturn(List.of(testUser));

        Map<String, String> request = Map.of(
                "status", "APPROVED",
                "resolutionNote", "신고 승인 처리"
        );

        // when
        ResponseEntity<?> response = controller.resolveReport(1L, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 처리 - 거부")
    void resolveReport_Reject() {
        // given
        given(reportRepository.findById(1L)).willReturn(Optional.of(testReport));
        given(userRepository.findAll()).willReturn(List.of(testUser));

        Map<String, String> request = Map.of(
                "status", "REJECTED",
                "resolutionNote", "신고 거부 처리"
        );

        // when
        ResponseEntity<?> response = controller.resolveReport(1L, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 처리 - 존재하지 않는 신고")
    void resolveReport_NonExistingReport() {
        // given
        given(reportRepository.findById(999L)).willReturn(Optional.empty());

        Map<String, String> request = Map.of(
                "status", "APPROVED",
                "resolutionNote", "신고 승인 처리"
        );

        // when
        ResponseEntity<?> response = controller.resolveReport(999L, request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("신고 통계 조회")
    void getReportStats() {
        // given
        given(reportRepository.countByIsResolvedFalse()).willReturn(5L);
        given(reportRepository.count()).willReturn(10L);

        // when
        ResponseEntity<Map<String, Object>> response = controller.getReportStats();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> stats = response.getBody();
        
        assertThat(stats).isNotNull();
        assertThat(stats.get("pending")).isEqualTo(5L);
        assertThat(stats.get("total")).isEqualTo(10L);
    }
} 