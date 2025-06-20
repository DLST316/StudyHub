package dev.kang.studyhub.service.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.web.user.UserJoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(UserJoinForm form) {
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        User user = User.builder()
                .name(form.getName())
                .email(form.getEmail())
                .password(encodedPassword)
                .university(form.getUniversity())
                .major(form.getMajor())
                .educationStatus(form.getEducationStatus())
                .role("USER")
                .build();

        userRepository.save(user);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }
}