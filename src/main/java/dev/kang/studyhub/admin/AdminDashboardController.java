package dev.kang.studyhub.admin;

import dev.kang.studyhub.board.repository.PostRepository;
import dev.kang.studyhub.board.repository.PostCommentRepository;
import dev.kang.studyhub.board.repository.BoardRepository;
import dev.kang.studyhub.board.repository.ReportRepository;
import dev.kang.studyhub.study.repository.StudyRepository;
import dev.kang.studyhub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자 대시보드 API
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final StudyRepository studyRepository;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;

    /**
     * 대시보드 통계 데이터
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 기본 통계
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPosts", postRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("totalStudies", studyRepository.count());
        stats.put("totalBoards", boardRepository.count());
        stats.put("pendingReports", reportRepository.countByIsResolvedFalse());
        
        // 차단된 유저 수
        stats.put("blockedUsers", userRepository.countByIsBlockedTrue());
        
        // 최근 7일간 활동 (추후 구현)
        // stats.put("recentUsers", userRepository.countByCreatedAtAfter(java.time.LocalDateTime.now().minusDays(7)));
        // stats.put("recentPosts", postRepository.countByCreatedAtAfter(java.time.LocalDateTime.now().minusDays(7)));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 최근 신고 내역
     */
    @GetMapping("/recent-reports")
    public ResponseEntity<?> getRecentReports() {
        return ResponseEntity.ok(reportRepository.findTop10ByOrderByReportedAtDesc());
    }
} 