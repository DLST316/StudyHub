package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.study.StudyCommentService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 스터디 댓글 관련 웹 요청을 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 댓글 작성 (POST /studies/{studyId}/comments)
 * - 댓글 수정 (PUT /studies/{studyId}/comments/{commentId})
 * - 댓글 삭제 (DELETE /studies/{studyId}/comments/{commentId})
 */
@Controller
@RequestMapping("/studies/{studyId}/comments")
@RequiredArgsConstructor
public class StudyCommentController {

    private final StudyCommentService studyCommentService;
    private final StudyService studyService;
    private final UserService userService;

    /**
     * 현재 로그인된 사용자를 가져오는 헬퍼 메서드
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }

    /**
     * 댓글 작성
     * POST /studies/{studyId}/comments
     */
    @PostMapping
    public String createComment(
            @PathVariable Long studyId,
            @RequestParam String content) {
        
        try {
            Study study = studyService.findById(studyId)
                    .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
            
            User currentUser = getCurrentUser();
            
            studyCommentService.createComment(content, currentUser, study);
            
            return "redirect:/studies/" + studyId;
        } catch (IllegalArgumentException e) {
            // 잘못된 요청 (스터디 없음, 입력값 오류 등)
            return "redirect:/studies/" + studyId + "?error=invalid";
        } catch (IllegalStateException e) {
            // 권한 없음
            return "redirect:/studies/" + studyId + "?error=permission";
        } catch (Exception e) {
            // 기타 예외
            return "redirect:/studies/" + studyId + "?error=unknown";
        }
    }

    /**
     * 댓글 수정
     * PUT /studies/{studyId}/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    @ResponseBody
    public String updateComment(
            @PathVariable Long studyId,
            @PathVariable Long commentId,
            @RequestParam String content) {
        
        User currentUser = getCurrentUser();
        
        try {
            studyCommentService.updateComment(commentId, content, currentUser);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /**
     * 댓글 삭제
     * DELETE /studies/{studyId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    @ResponseBody
    public String deleteComment(
            @PathVariable Long studyId,
            @PathVariable Long commentId) {
        
        User currentUser = getCurrentUser();
        
        try {
            studyCommentService.deleteComment(commentId, currentUser);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
} 