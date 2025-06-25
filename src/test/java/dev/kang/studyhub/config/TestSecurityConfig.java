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
 * 실제 운영 환경과 동일한 설정을 사용하여 테스트의 정확성을 높입니다.
 * - 실제 SecurityConfig와 동일한 URL별 접근 권한 설정
 * - CSRF 보호 비활성화 (테스트 편의)
 * - 폼 로그인 비활성화 (테스트에서는 @WithMockUser 사용)
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 실제 SecurityConfig와 동일한 설정
                .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
                .requestMatchers("/api/images/**").authenticated()
                // 그 외 모든 요청은 인증이 필요 (실제 설정과 동일)
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화 (테스트 편의)
            .formLogin(form -> form.disable())  // 폼 로그인 비활성화 (테스트에서는 @WithMockUser 사용)
            .logout(logout -> logout.disable());  // 로그아웃 비활성화
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 