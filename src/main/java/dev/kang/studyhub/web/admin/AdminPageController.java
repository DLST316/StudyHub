package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
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
    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final StudyRepository studyRepository;

    /**
     * 관리자 대시보드
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "admin/dashboard";
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
     * 시스템 설정 페이지
     */
    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
} 