package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.session.SessionConst;
import dev.kang.studyhub.web.session.UserSessionDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/join")
    public String showJoinForm(Model model) {
        model.addAttribute("userJoinForm", new UserJoinForm());
        return "user/join";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new UserLoginForm());
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") UserLoginForm form,
                        BindingResult result,
                        HttpServletRequest request) {

        Optional<User> userOpt = userService.findByEmail(form.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(form.getPassword(), userOpt.get().getPassword())) {
            result.reject("loginFail", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "user/login";
        }

        // 로그인 성공
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_USER, new UserSessionDto(userOpt.get()));
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
    @PostMapping("/join")
    public String processJoin(@Valid @ModelAttribute("userJoinForm") UserJoinForm form,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "user/join";
        }

        // 이메일 중복 검사
        if (userService.findByEmail(form.getEmail()).isPresent()) {
            result.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
            return "user/join";
        }

        // 유저 생성 및 저장
        User user = User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .university(form.getUniversity())
                .major(form.getMajor())
                .role("USER")
                .educationStatus(form.getEducationStatus()) // ENUM
                .build();

        userService.save(user);
        return "redirect:/login";
    }
}