package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.board.ReportRepository;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 관리자 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final StudyRepository studyRepository;
    private final ReportRepository reportRepository;

    /**
     * 관리자 대시보드
     */
    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    /**
     * 사용자 관리 페이지
     */
    @GetMapping("/users")
    public String users(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<?> users = userRepository.findAll(pageable);
        model.addAttribute("users", users);
        return "admin/users";
    }

    /**
     * 차단된 사용자 목록
     */
    @GetMapping("/users/blocked")
    public String blockedUsers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        // TODO: 차단된 사용자만 조회하는 메서드 구현 필요
        Page<?> users = userRepository.findAll(pageable);
        model.addAttribute("users", users);
        model.addAttribute("isBlockedList", true);
        return "admin/users";
    }

    /**
     * 게시글 관리 페이지
     */
    @GetMapping("/posts")
    public String posts(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<?> posts = postRepository.findAll(pageable);
        model.addAttribute("posts", posts);
        return "admin/posts";
    }

    /**
     * 댓글 관리 페이지
     */
    @GetMapping("/comments")
    public String comments(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<?> comments = commentRepository.findAll(pageable);
        model.addAttribute("comments", comments);
        return "admin/comments";
    }

    /**
     * 스터디 관리 페이지
     */
    @GetMapping("/studies")
    public String studies(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<?> studies = studyRepository.findAll(pageable);
        model.addAttribute("studies", studies);
        return "admin/studies";
    }

    /**
     * 신고 관리 페이지
     */
    @GetMapping("/reports")
    public String reports(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<?> reports = reportRepository.findAll(pageable);
        model.addAttribute("reports", reports);
        return "admin/reports";
    }

    /**
     * 대기중인 신고 목록
     */
    @GetMapping("/reports/pending")
    public String pendingReports(@PageableDefault(size = 20) Pageable pageable, Model model) {
        // TODO: 대기중인 신고만 조회하는 메서드 구현 필요
        Page<?> reports = reportRepository.findAll(pageable);
        model.addAttribute("reports", reports);
        model.addAttribute("isPendingList", true);
        return "admin/reports";
    }

    /**
     * 시스템 설정 페이지
     */
    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
} 