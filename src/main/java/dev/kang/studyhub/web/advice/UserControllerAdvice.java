package dev.kang.studyhub.web.advice;

import dev.kang.studyhub.service.user.exception.AlreadyExistsEmailException;
import dev.kang.studyhub.web.user.UserJoinForm;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserControllerAdvice {

    @ExceptionHandler(AlreadyExistsEmailException.class)
    public String handleAlreadyExistsEmail(AlreadyExistsEmailException ex, @ModelAttribute("userJoinForm") UserJoinForm form, BindingResult bindingResult, Model model) {
        bindingResult.rejectValue("email", "duplicate", ex.getMessage());
        return "user/join";
    }
} 