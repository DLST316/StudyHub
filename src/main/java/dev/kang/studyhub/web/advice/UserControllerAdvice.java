package dev.kang.studyhub.web.advice;

import dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException;
import dev.kang.studyhub.web.user.UserJoinForm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리를 담당하는 컨트롤러 어드바이스
 * 
 * 주요 기능:
 * - 사용자 관련 예외 처리 (이메일 중복 등)
 * - 스터디 관련 예외 처리 (존재하지 않는 스터디, 권한 없음 등)
 * - 일반적인 비즈니스 예외 처리
 */
@ControllerAdvice
public class UserControllerAdvice {

    /**
     * 이메일 중복 예외 처리
     * 회원가입 폼에 에러 메시지를 표시하고 다시 폼을 보여줌
     */
    @ExceptionHandler(AlreadyExistsEmailException.class)
    public String handleAlreadyExistsEmail(AlreadyExistsEmailException ex, 
                                         @ModelAttribute("userJoinForm") UserJoinForm form, 
                                         BindingResult bindingResult, 
                                         Model model) {
        bindingResult.rejectValue("email", "duplicate", ex.getMessage());
        return "user/join";
    }

    /**
     * 파일 파라미터 누락 예외 처리
     * API 요청의 경우 JSON 응답, 웹 요청의 경우 에러 페이지 반환
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "필수 파라미터가 누락되었습니다: " + ex.getRequestPartName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 스터디를 찾을 수 없는 경우 예외 처리
     * 에러 페이지로 리다이렉트
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        mav.addObject("error", "요청한 리소스를 찾을 수 없습니다.");
        return mav;
    }

    /**
     * 권한 없음, 중복 신청 등 비즈니스 규칙 위반 예외 처리
     * 에러 페이지로 리다이렉트
     */
    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(IllegalStateException ex) {
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("message", ex.getMessage());
        mav.addObject("error", "잘못된 요청입니다.");
        return mav;
    }

    /**
     * 기타 예상치 못한 예외 처리
     * 서버 에러 페이지로 리다이렉트
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "서버 내부 오류가 발생했습니다.");
        mav.addObject("error", "일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return mav;
    }

} 