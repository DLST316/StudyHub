package dev.kang.studyhub.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 로그인 관련 요청을 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 로그인 페이지 표시
 * - 로그인 에러 메시지 처리
 * - 로그아웃 메시지 처리
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    /**
     * 로그인 페이지를 표시하는 메서드
     * 
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @param error 로그인 실패 시 전달되는 파라미터
     * @param logout 로그아웃 시 전달되는 파라미터
     * @return 로그인 페이지 템플릿 이름 ("login")
     */
    @GetMapping("/login")
    public String loginPage(Model model, 
                          @RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout) {
        
        // 로그인 실패 시 에러 메시지 설정
        if (error != null) {
            model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        // 로그아웃 시 성공 메시지 설정
        if (logout != null) {
            model.addAttribute("successMessage", "로그아웃되었습니다.");
        }
        
        // 로그인 페이지 템플릿 반환
        return "login";
    }
} 