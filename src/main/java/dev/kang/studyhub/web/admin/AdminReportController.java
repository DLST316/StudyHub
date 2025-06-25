package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.Report;
import dev.kang.studyhub.domain.board.ReportRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.web.admin.dto.ReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 신고 처리 API
 */
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * 신고 관리 페이지
     */
    @GetMapping
    public String reportsPage() {
        return "admin/reports";
    }

    /**
     * 대기중인 신고 목록 페이지
     */
    @GetMapping("/pending")
    public String pendingReportsPage() {
        return "admin/reports";
    }

    /**
     * 신고 목록 조회 (페이지네이션)
     */
    @GetMapping("/api")
    @ResponseBody
    public Page<ReportDto> getReportsApi(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Report.ReportStatus status,
            @RequestParam(required = false) Report.ReportTargetType targetType) {
        
        Page<Report> reportPage;
        if (status != null) {
            reportPage = reportRepository.findByStatus(status, pageable);
        } else if (targetType != null) {
            reportPage = reportRepository.findByTargetType(targetType, pageable);
        } else {
            reportPage = reportRepository.findAll(pageable);
        }
        
        List<ReportDto> reportDtos = reportPage.getContent().stream()
                .map(ReportDto::from)
                .collect(Collectors.toList());
        
        return new PageImpl<>(reportDtos, pageable, reportPage.getTotalElements());
    }

    /**
     * 신고 상세 조회
     */
    @GetMapping("/api/{reportId}")
    @ResponseBody
    public ResponseEntity<ReportDto> getReport(@PathVariable Long reportId) {
        return reportRepository.findById(reportId)
                .map(ReportDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 신고 처리 (승인/거부)
     */
    @PostMapping("/api/{reportId}/resolve")
    @ResponseBody
    public ResponseEntity<?> resolveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {
        
        Report.ReportStatus status = Report.ReportStatus.valueOf(request.get("status"));
        String resolutionNote = request.get("resolutionNote");
        
        return reportRepository.findById(reportId)
                .map(report -> {
                    // TODO: 현재 로그인한 관리자 정보를 가져와야 함
                    // 임시로 첫 번째 유저를 관리자로 사용
                    User admin = userRepository.findAll().get(0);
                    
                    report.resolve(status, admin, resolutionNote);
                    reportRepository.save(report);
                    
                    return ResponseEntity.ok(Map.of("message", "신고가 처리되었습니다."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 신고 통계
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReportStats() {
        Map<String, Object> stats = Map.of(
            "pending", reportRepository.countByIsResolvedFalse(),
            "total", reportRepository.count()
        );
        return ResponseEntity.ok(stats);
    }
} 