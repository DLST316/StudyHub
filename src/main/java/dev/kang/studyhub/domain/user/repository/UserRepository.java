package dev.kang.studyhub.domain.user.repository;

import dev.kang.studyhub.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);   // 로그인 등에 사용
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}