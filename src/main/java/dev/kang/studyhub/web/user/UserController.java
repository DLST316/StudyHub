package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.session.SessionConst;
import dev.kang.studyhub.web.session.UserSessionDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 사용자 관련 기능을 처리하는 컨트롤러
 * 회원가입, 로그아웃 등의 사용자 인증 관련 기능을 담당합니다.
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    // UserService를 주입받습니다 (Lombok의 @RequiredArgsConstructor가 자동으로 생성)
    private final UserService userService;

    /**
     * 회원가입 폼을 보여주는 메서드
     * GET /join 요청을 처리합니다.
     * 
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return 회원가입 폼 템플릿 이름 ("user/join")
     */
    @GetMapping("/join")
    public String showJoinForm(Model model) {
        // 새로운 UserJoinForm 객체를 생성하여 모델에 추가
        // 이렇게 하면 Thymeleaf에서 폼 바인딩을 할 수 있습니다
        model.addAttribute("userJoinForm", new UserJoinForm());
        return "user/join";
    }

    /**
     * 로그아웃을 처리하는 메서드
     * GET /logout 요청을 처리합니다.
     * 
     * @param request HTTP 요청 객체 (세션 정보에 접근하기 위해 사용)
     * @return 홈페이지로 리다이렉트 ("redirect:/")
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // 현재 세션을 가져옵니다 (false: 세션이 없으면 null 반환)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 세션이 존재하면 세션을 무효화하여 로그아웃 처리
            session.invalidate();
        }
        // 홈페이지로 리다이렉트
        return "redirect:/";
    }

    /**
     * 회원가입 처리를 하는 메서드
     * POST /join 요청을 처리합니다.
     * 
     * @param form 사용자가 입력한 회원가입 정보 (UserJoinForm 객체)
     * @param result 폼 검증 결과를 담는 객체 (BindingResult)
     * @return 성공 시 홈페이지로 리다이렉트, 실패 시 회원가입 폼으로 다시 이동
     */
    @PostMapping("/join")
    public String processJoin(@Valid @ModelAttribute("userJoinForm") UserJoinForm form,
                              BindingResult result) {
        // @Valid: 폼 데이터의 유효성 검사를 수행합니다
        // @ModelAttribute: HTTP 요청 파라미터를 UserJoinForm 객체로 바인딩합니다
        
        // 폼 검증에 오류가 있는 경우
        if (result.hasErrors()) {
            // 회원가입 폼으로 다시 이동 (오류 메시지와 함께)
            return "user/join";
        }

        try {
            // 폼 검증이 성공한 경우 UserService를 통해 회원가입 처리
            userService.join(form);
            
            // 회원가입 성공 후 홈페이지로 리다이렉트 (성공 메시지와 함께)
            return "redirect:/?success=join";
        } catch (dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException e) {
            // 중복 이메일 예외 처리
            result.rejectValue("email", "duplicate.email", e.getMessage());
            return "user/join";
        }
    }
}