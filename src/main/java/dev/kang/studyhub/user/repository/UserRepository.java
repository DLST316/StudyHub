package dev.kang.studyhub.user.repository;

import dev.kang.studyhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 사용자명(아이디)으로 사용자 조회 (로그인용)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 사용자명(아이디) 존재 여부 확인
     */
    boolean existsByUsername(String username);
    
    /**
     * 사용자명(아이디)으로 사용자 삭제
     */
    void deleteByUsername(String username);
    
    /**
     * 이메일로 사용자 조회 (연락처용)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 이메일로 사용자 삭제
     */
    void deleteByEmail(String email);
    
    /**
     * 차단된 유저 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isBlocked = true")
    long countByIsBlockedTrue();
    
    /**
     * 사용자명 또는 이름으로 검색 (대소문자 무시)
     */
    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(
        String username, String name, Pageable pageable);
    
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