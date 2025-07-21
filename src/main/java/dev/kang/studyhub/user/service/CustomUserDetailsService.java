package dev.kang.studyhub.user.service;

import dev.kang.studyhub.user.entity.User;
import dev.kang.studyhub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security와 우리 애플리케이션의 사용자 정보를 연결하는 서비스
 * 
 * Spring Security는 사용자 인증 시 UserDetailsService를 통해 사용자 정보를 조회합니다.
 * 이 클래스는 우리가 만든 User 엔티티를 Spring Security가 이해할 수 있는 UserDetails로 변환합니다.
 * 
 * 주요 역할:
 * - 사용자명(아이디)으로 사용자 조회 (로그인용)
 * - User 엔티티를 UserDetails로 변환
 * - Spring Security 인증 시스템과 연동
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 사용자 정보를 조회하기 위한 리포지토리
    private final UserRepository userRepository;

    /**
     * 사용자명(아이디)으로 사용자 정보를 조회하는 메서드
     * Spring Security가 로그인 시 이 메서드를 호출하여 사용자 정보를 가져옵니다.
     * 
     * @param username 사용자명(아이디) (Spring Security에서는 username으로 전달됨)
     * @return Spring Security가 사용할 수 있는 UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생하는 예외
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자명으로 사용자 조회
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        // Spring Security의 UserDetails 객체를 생성하여 반환
        // 이 객체는 Spring Security가 인증과 권한 관리를 위해 사용합니다
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())     // 사용자 식별자 (사용자명)
            .password(user.getPassword())     // 암호화된 비밀번호
            .roles(user.getRole())            // 사용자 역할 (예: "USER", "ADMIN")
            .build();
    }
} 