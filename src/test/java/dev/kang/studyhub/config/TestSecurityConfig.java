package dev.kang.studyhub.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트 환경을 위한 Spring Security 설정
 * 
 * 실제 운영 환경과는 다른 설정을 사용하여 테스트를 단순화합니다.
 * - 모든 요청을 허용하여 인증 없이 테스트할 수 있도록 설정
 * - CSRF 보호 비활성화
 * - 기본적인 보안 설정만 유지
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // 모든 요청 허용
            )
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화
            .formLogin(form -> form.disable())  // 폼 로그인 비활성화
            .logout(logout -> logout.disable());  // 로그아웃 비활성화
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 