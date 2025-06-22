package dev.kang.studyhub.web;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.service.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 홈페이지 컨트롤러
 * 메인 페이지(/)를 처리하는 컨트롤러입니다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final StudyService studyService;
    
    /**
     * 홈페이지 메인 화면을 처리하는 메서드
     * 
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @param success URL 파라미터로 전달되는 성공 메시지 타입 (예: "join" - 회원가입 성공)
     * @return 홈페이지 템플릿 이름 ("home")
     */
    @GetMapping("/")
    public String home(Model model, @RequestParam(value = "success", required = false) String success) {
        
        // Spring Security에서 현재 인증된 사용자 정보를 가져옵니다
        // SecurityContextHolder는 Spring Security가 관리하는 보안 컨텍스트에 접근하는 방법입니다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 사용자 로그인 상태를 확인합니다
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            // 로그인된 사용자인 경우
            // authentication.isAuthenticated(): 사용자가 인증되었는지 확인
            // authentication.getName(): 사용자의 식별자(이메일)를 가져옴
            // "anonymousUser"는 Spring Security가 로그인하지 않은 사용자에게 부여하는 기본값
            
            model.addAttribute("isLoggedIn", true);  // 템플릿에서 로그인 상태를 확인할 수 있도록
            model.addAttribute("userEmail", authentication.getName());  // 사용자 이메일을 템플릿에 전달
        } else {
            // 로그인되지 않은 사용자인 경우
            model.addAttribute("isLoggedIn", false);
        }
        
        // 성공 메시지 처리
        // 회원가입 완료 후 리다이렉트될 때 전달되는 파라미터를 처리
        if ("join".equals(success)) {
            model.addAttribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요.");
        } else if ("login".equals(success)) {
            model.addAttribute("successMessage", "로그인되었습니다.");
        }
        
        // 최근 스터디 목록 조회 (최대 6개)
        List<Study> recentStudies = studyService.findAll(PageRequest.of(0, 6)).getContent();
        model.addAttribute("recentStudies", recentStudies);
        
        // "home" 템플릿을 렌더링하여 반환
        return "home";
    }
} 