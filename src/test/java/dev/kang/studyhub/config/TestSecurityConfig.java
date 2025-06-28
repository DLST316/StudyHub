package dev.kang.studyhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트 환경을 위한 Spring Security 설정
 * 
 * 테스트 편의를 위해 보안 설정을 완화하되, 실제 보안 동작도 테스트할 수 있도록 설정합니다.
 * - CSRF 보호 완전 비활성화
 * - 실제 URL별 접근 권한 설정 유지
 * - 폼 로그인/로그아웃 활성화 (테스트에서 사용)
 */
@Configuration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 실제 SecurityConfig와 동일한 URL별 접근 권한 설정
                .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/images/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)  // CSRF 완전 비활성화
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/?success=login")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            )
            .headers(headers -> headers
                .frameOptions().disable()  // H2 콘솔 사용을 위해
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 