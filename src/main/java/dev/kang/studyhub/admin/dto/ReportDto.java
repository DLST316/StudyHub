package dev.kang.studyhub.admin.dto;

import dev.kang.studyhub.board.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신고 정보 DTO
 * API 응답에서 사용하는 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String reporterName;
    private String targetType;
    private String targetTitle;
    private String reason;
    private String status;
    private String resolutionNote;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    /**
     * Report 엔티티를 ReportDto로 변환
     */
    public static ReportDto from(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .reporterName(report.getReporter() != null ? report.getReporter().getName() : "알 수 없음")
                .targetType(report.getTargetType() != null ? report.getTargetType().name() : "알 수 없음")
                .targetTitle("ID: " + report.getTargetId())
                .reason(report.getReason() != null ? report.getReason().name() : "알 수 없음")
                .status(report.getStatus() != null ? report.getStatus().name() : "PENDING")
                .resolutionNote(report.getResolutionNote())
                .adminName(report.getResolver() != null ? report.getResolver().getName() : null)
                .createdAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
} 