package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportRepository 통합 테스트
 *
 * - 미처리 신고 수 조회
 * - 최근 신고 내역 조회
 * - 상태별 신고 목록 조회
 * - 신고 대상 타입별 조회
 * - 신고자별 신고 내역 조회
 * - 특정 대상에 대한 신고 내역 조회
 * - 처리자별 처리 내역 조회
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReportRepositoryTest {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private User reporter1;
    private User reporter2;
    private User resolver;
    private Report report1;
    private Report report2;
    private Report report3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        reporter1 = User.builder()
                .name("신고자1")
                .email("reporter1@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(reporter1);

        reporter2 = User.builder()
                .name("신고자2")
                .email("reporter2@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(reporter2);

        resolver = User.builder()
                .name("처리자")
                .email("resolver@test.com")
                .password("password")
                .role("ADMIN")
                .isBlocked(false)
                .build();
        userRepository.save(resolver);

        // 신고 데이터 생성
        report1 = new Report();
        report1.setReporter(reporter1);
        report1.setTargetType(Report.ReportTargetType.POST);
        report1.setTargetId(1L);
        report1.setReason(Report.ReportReason.HARASSMENT);
        report1.setStatus(Report.ReportStatus.PENDING);
        report1.setDescription("부적절한 게시글입니다.");
        report1.setReportedAt(LocalDateTime.now().minusDays(1));
        reportRepository.save(report1);

        report2 = new Report();
        report2.setReporter(reporter2);
        report2.setTargetType(Report.ReportTargetType.COMMENT);
        report2.setTargetId(2L);
        report2.setReason(Report.ReportReason.SPAM);
        report2.setStatus(Report.ReportStatus.RESOLVED);
        report2.setDescription("스팸 댓글입니다.");
        report2.setReportedAt(LocalDateTime.now().minusHours(6));
        report2.setResolver(resolver);
        report2.setResolvedAt(LocalDateTime.now().minusHours(2));
        report2.setResolutionNote("스팸으로 판단되어 삭제 처리했습니다.");
        reportRepository.save(report2);

        report3 = new Report();
        report3.setReporter(reporter1);
        report3.setTargetType(Report.ReportTargetType.USER);
        report3.setTargetId(3L);
        report3.setReason(Report.ReportReason.INAPPROPRIATE_CONTENT);
        report3.setStatus(Report.ReportStatus.PENDING);
        report3.setDescription("부적절한 사용자입니다.");
        report3.setReportedAt(LocalDateTime.now().minusHours(2));
        reportRepository.save(report3);
    }

    @Test
    @DisplayName("미처리 신고 수 조회")
    void countByIsResolvedFalse() throws Exception {
        // when
        long count = reportRepository.countByIsResolvedFalse();

        // then
        assertThat(count).isEqualTo(2); // PENDING 상태인 report1, report3
    }

    @Test
    @DisplayName("최근 신고 내역 조회 (최신순)")
    void findTop10ByOrderByReportedAtDesc() throws Exception {
        // when
        List<Report> reports = reportRepository.findTop10ByOrderByReportedAtDesc();

        // then
        assertThat(reports).hasSize(3);
        assertThat(reports.get(0).getReportedAt()).isAfter(reports.get(1).getReportedAt());
        assertThat(reports.get(1).getReportedAt()).isAfter(reports.get(2).getReportedAt());
    }

    @Test
    @DisplayName("상태별 신고 목록 조회 - PENDING")
    void findByStatus_pending() throws Exception {
        // when
        Page<Report> page = reportRepository.findByStatus(
                Report.ReportStatus.PENDING, 
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).allMatch(report -> 
                report.getStatus() == Report.ReportStatus.PENDING);
    }

    @Test
    @DisplayName("상태별 신고 목록 조회 - RESOLVED")
    void findByStatus_resolved() throws Exception {
        // when
        Page<Report> page = reportRepository.findByStatus(
                Report.ReportStatus.RESOLVED, 
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(Report.ReportStatus.RESOLVED);
    }

    @Test
    @DisplayName("신고 대상 타입별 조회 - POST")
    void findByTargetType_post() throws Exception {
        // when
        Page<Report> page = reportRepository.findByTargetType(
                Report.ReportTargetType.POST, 
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTargetType()).isEqualTo(Report.ReportTargetType.POST);
    }

    @Test
    @DisplayName("신고자별 신고 내역 조회")
    void findByReporterId() throws Exception {
        // when
        Page<Report> page = reportRepository.findByReporterId(
                reporter1.getId(), 
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(2); // reporter1이 작성한 report1, report3
        assertThat(page.getContent()).allMatch(report -> 
                report.getReporter().getId().equals(reporter1.getId()));
    }

    @Test
    @DisplayName("특정 대상에 대한 신고 내역 조회")
    void findByTarget() throws Exception {
        // when
        List<Report> reports = reportRepository.findByTarget(
                Report.ReportTargetType.POST, 
                1L
        );

        // then
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getTargetType()).isEqualTo(Report.ReportTargetType.POST);
        assertThat(reports.get(0).getTargetId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("처리자별 처리 내역 조회")
    void findByResolverId() throws Exception {
        // when
        Page<Report> page = reportRepository.findByResolverId(
                resolver.getId(), 
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(1); // resolver가 처리한 report2
        assertThat(page.getContent().get(0).getResolver().getId()).isEqualTo(resolver.getId());
    }
} 