package dev.kang.studyhub.domain.user.repository;

import dev.kang.studyhub.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);   // 로그인 등에 사용
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
    
    /**
     * 차단된 유저 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isBlocked = true")
    long countByIsBlockedTrue();
    
    /**
     * 이메일 또는 이름으로 검색 (대소문자 무시)
     */
    Page<User> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
        String email, String name, Pageable pageable);
    
    /**
     * 차단된 사용자 목록 조회
     */
    Page<User> findByIsBlockedTrue(Pageable pageable);
    
    /**
     * 활성 사용자 목록 조회
     */
    Page<User> findByIsBlockedFalse(Pageable pageable);
}