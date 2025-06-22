package dev.kang.studyhub.web.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.study.StudyApplicationService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스터디 관련 웹 요청을 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 스터디 목록 조회 (/studies)
 * - 스터디 상세 조회 (/studies/{id})
 * - 스터디 생성 폼 (/studies/new)
 * - 스터디 수정 폼 (/studies/edit/{id})
 * - 스터디 삭제 (/studies/delete/{id})
 */
@Controller
@RequestMapping("/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final UserService userService;
    private final StudyApplicationService studyApplicationService;

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
     * 스터디 목록 페이지
     * GET /studies
     */
    @GetMapping
    public String listStudies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Study> studies;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색
            List<Study> searchResults = studyService.searchByTitle(keyword);
            studies = Page.empty(pageable); // 페이징은 나중에 구현
            model.addAttribute("studies", searchResults);
            model.addAttribute("keyword", keyword);
        } else {
            // 전체 목록
            studies = studyService.findAll(pageable);
            model.addAttribute("studies", studies.getContent());
        }
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studies.getTotalPages());
        model.addAttribute("totalElements", studies.getTotalElements());
        
        return "study/list";
    }

    /**
     * 스터디 상세 페이지
     * GET /studies/{id}
     */
    @GetMapping("/{id}")
    public String viewStudy(@PathVariable Long id, Model model) {
        Study study = studyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        // 현재 사용자 정보 가져오기
        try {
            User currentUser = getCurrentUser();
            model.addAttribute("isLeader", study.isLeader(currentUser));
            
            // 신청 상태 확인
            boolean isApplied = studyApplicationService.hasApplied(currentUser, study);
            model.addAttribute("isApplied", isApplied);
            
            // 신청 상태가 있다면 상태 정보도 추가
            if (isApplied) {
                var application = studyApplicationService.findByUserAndStudy(currentUser, study);
                if (application.isPresent()) {
                    model.addAttribute("applicationStatus", application.get().getStatus());
                }
            }
        } catch (IllegalStateException e) {
            model.addAttribute("isLeader", false);
            model.addAttribute("isApplied", false);
        }
        
        model.addAttribute("study", study);
        return "study/detail";
    }

    /**
     * 스터디 생성 폼 페이지
     * GET /studies/new
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("studyForm", new StudyForm());
        return "study/form";
    }

    /**
     * 스터디 생성 처리
     * POST /studies/new
     */
    @PostMapping("/new")
    public String createStudy(@ModelAttribute StudyForm studyForm) {
        User currentUser = getCurrentUser();
        
        Study study = Study.builder()
                .title(studyForm.getTitle())
                .description(studyForm.getDescription())
                .leader(currentUser)
                .recruitmentLimit(studyForm.getRecruitmentLimit())
                .requirement(studyForm.getRequirement())
                .deadline(studyForm.getDeadline())
                .build();
        
        studyService.createStudy(study);
        
        return "redirect:/studies/" + study.getId();
    }

    /**
     * 스터디 수정 폼 페이지
     * GET /studies/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Study study = studyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        User currentUser = getCurrentUser();
        if (!study.isLeader(currentUser)) {
            throw new IllegalStateException("스터디 개설자만 수정할 수 있습니다.");
        }
        
        StudyForm studyForm = new StudyForm();
        studyForm.setTitle(study.getTitle());
        studyForm.setDescription(study.getDescription());
        studyForm.setRecruitmentLimit(study.getRecruitmentLimit());
        studyForm.setRequirement(study.getRequirement());
        studyForm.setDeadline(study.getDeadline());
        
        model.addAttribute("studyForm", studyForm);
        model.addAttribute("studyId", id);
        
        return "study/form";
    }

    /**
     * 스터디 수정 처리
     * POST /studies/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String updateStudy(@PathVariable Long id, @ModelAttribute StudyForm studyForm) {
        Study study = studyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        User currentUser = getCurrentUser();
        if (!study.isLeader(currentUser)) {
            throw new IllegalStateException("스터디 개설자만 수정할 수 있습니다.");
        }
        
        studyService.updateStudy(
                study,
                studyForm.getTitle(),
                studyForm.getDescription(),
                studyForm.getRecruitmentLimit(),
                studyForm.getRequirement(),
                studyForm.getDeadline()
        );
        
        return "redirect:/studies/" + id;
    }

    /**
     * 스터디 삭제 처리
     * POST /studies/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteStudy(@PathVariable Long id) {
        Study study = studyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
        
        User currentUser = getCurrentUser();
        if (!study.isLeader(currentUser)) {
            throw new IllegalStateException("스터디 개설자만 삭제할 수 있습니다.");
        }
        
        studyService.deleteStudy(study);
        
        return "redirect:/studies";
    }

    /**
     * 내가 개설한 스터디 목록
     * GET /studies/my
     */
    @GetMapping("/my")
    public String myStudies(Model model) {
        User currentUser = getCurrentUser();
        List<Study> myStudies = studyService.findByLeader(currentUser);
        model.addAttribute("studies", myStudies);
        return "study/my-studies";
    }
} 