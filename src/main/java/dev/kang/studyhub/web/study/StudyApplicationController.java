package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.study.repository.StudyApplicationRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.study.StudyApplicationService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스터디 신청 관련 웹 요청을 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 스터디 신청 (/studies/{studyId}/apply)
 * - 스터디 신청 취소 (/studies/{studyId}/cancel)
 * - 스터디 신청자 목록 (/studies/{studyId}/applications)
 * - 신청 수락/거절 (/studies/{studyId}/applications/{applicationId}/accept, /reject)
 */
@Controller
@RequestMapping("/studies")
@RequiredArgsConstructor
public class StudyApplicationController {

    private final StudyService studyService;
    private final StudyApplicationService applicationService;
    private final StudyApplicationRepository applicationRepository;
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
        
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }

    /**
     * 스터디 신청 처리
     * POST /studies/{studyId}/apply
     */
    @PostMapping("/{studyId}/apply")
    public String applyForStudy(@PathVariable Long studyId) {
        User user = getCurrentUser();
        
        Study study = studyService.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        // 이미 신청했는지 확인
        if (applicationService.hasApplied(user, study)) {
            throw new IllegalStateException("이미 신청한 스터디입니다.");
        }
        
        // 스터디 개설자는 신청할 수 없음
        if (study.isLeader(user)) {
            throw new IllegalStateException("스터디 개설자는 신청할 수 없습니다.");
        }
        
        applicationService.apply(user, study);
        
        return "redirect:/studies/" + studyId + "?success=applied";
    }

    /**
     * 스터디 신청 취소 처리
     * POST /studies/{studyId}/cancel
     */
    @PostMapping("/{studyId}/cancel")
    public String cancelApplication(@PathVariable Long studyId) {
        User user = getCurrentUser();
        
        Study study = studyService.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        StudyApplication application = applicationRepository.findByUserAndStudy(user, study)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));
        
        applicationService.cancel(application.getId(), user);
        
        return "redirect:/studies/" + studyId + "?success=cancelled";
    }

    /**
     * 스터디 신청자 목록 페이지 (스터디 개설자만 접근 가능)
     * GET /studies/{studyId}/applications
     */
    @GetMapping("/{studyId}/applications")
    public String listApplications(@PathVariable Long studyId, Model model,
                                 @RequestParam(value = "success", required = false) String success,
                                 @RequestParam(value = "error", required = false) String error) {
        User user = getCurrentUser();
        
        Study study = studyService.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        // 스터디 개설자만 접근 가능
        if (!study.isLeader(user)) {
            throw new IllegalStateException("스터디 개설자만 접근할 수 있습니다.");
        }
        
        List<StudyApplication> applications = applicationService.findByStudy(study);
        
        // 승인된 신청자 수 계산
        long approvedCount = applicationService.countApprovedApplications(study);
        
        model.addAttribute("study", study);
        model.addAttribute("applications", applications);
        model.addAttribute("approvedCount", approvedCount);
        
        // 성공/에러 메시지 처리
        if (success != null) {
            if ("accepted".equals(success)) {
                model.addAttribute("successMessage", "신청이 승인되었습니다.");
            } else if ("rejected".equals(success)) {
                model.addAttribute("successMessage", "신청이 거절되었습니다.");
            }
        }
        
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        
        return "study/applications";
    }

    /**
     * 스터디 신청 수락 처리
     * POST /studies/{studyId}/applications/{applicationId}/accept
     */
    @PostMapping("/{studyId}/applications/{applicationId}/accept")
    public String acceptApplication(@PathVariable Long studyId, @PathVariable Long applicationId) {
        User user = getCurrentUser();
        Study study = studyService.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        // 스터디 개설자만 수락할 수 있음
        if (!study.isLeader(user)) {
            throw new IllegalStateException("스터디 개설자만 수락할 수 있습니다.");
        }
        // 신청 내역 존재 여부 체크
        StudyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));
        
        try {
            applicationService.approve(applicationId);
            return "redirect:/studies/" + studyId + "/applications?success=accepted";
        } catch (IllegalStateException e) {
            // 인원 제한 등의 비즈니스 로직 예외 처리
            return "redirect:/studies/" + studyId + "/applications?error=" + e.getMessage();
        }
    }

    /**
     * 스터디 신청 거절 처리
     * POST /studies/{studyId}/applications/{applicationId}/reject
     */
    @PostMapping("/{studyId}/applications/{applicationId}/reject")
    public String rejectApplication(@PathVariable Long studyId, @PathVariable Long applicationId) {
        User user = getCurrentUser();
        Study study = studyService.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        // 스터디 개설자만 거절할 수 있음
        if (!study.isLeader(user)) {
            throw new IllegalStateException("스터디 개설자만 거절할 수 있습니다.");
        }
        // 신청 내역 존재 여부 체크
        StudyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));
        applicationService.reject(applicationId);
        return "redirect:/studies/" + studyId + "/applications?success=rejected";
    }

    /**
     * 내가 신청한 스터디 목록
     * GET /applications/my
     */
    @GetMapping("/applications/my")
    public String myApplications(Model model) {
        User user = getCurrentUser();
        List<StudyApplication> myApplications = applicationService.findByUser(user);
        model.addAttribute("applications", myApplications);
        return "study/my-applications";
    }
} 