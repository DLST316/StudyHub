package dev.kang.studyhub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 설정 클래스
 * 애플리케이션의 보안 설정을 담당합니다.
 * 
 * 주요 기능:
 * - URL별 접근 권한 설정
 * - 로그인/로그아웃 설정
 * - 비밀번호 암호화 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Spring Security의 핵심 설정을 담당하는 메서드
     * HTTP 요청에 대한 보안 규칙을 정의합니다.
     * 
     * @param http HttpSecurity 객체 (Spring Security 설정을 위한 빌더)
     * @return SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화
                // 개발 환경에서는 편의를 위해 비활성화하지만, 운영 환경에서는 활성화해야 합니다
                .csrf(AbstractHttpConfigurer::disable)
                
                // H2 콘솔의 iframe 사용을 허용
                // H2 데이터베이스 콘솔이 iframe 내에서 정상 작동하도록 설정
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 다음 URL들은 인증 없이 접근 가능 (permitAll)
                        .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**","/h2-console/**").permitAll()
                        // 이미지 업로드 API는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/images/**").authenticated()
                        // 어드민 페이지는 ADMIN 권한만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 그 외 모든 요청은 인증이 필요 (authenticated)
                        .anyRequest().authenticated()
                )
                
                // 폼 로그인 설정
                .formLogin(form -> form
                        // 커스텀 로그인 페이지 설정
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // 로그인 시 사용할 username 파라미터 이름을 "username"으로 설정
                        // 기본값은 "username"이며, 우리는 사용자명(아이디)을 사용자 식별자로 사용
                        .usernameParameter("username")
                        .passwordParameter("password")
                        // 로그인 성공 시 리다이렉트할 URL
                        .defaultSuccessUrl("/?success=login")
                        // 로그인 실패 시 리다이렉트할 URL
                        .failureUrl("/login?error=true")
                        // 로그인 페이지 접근을 모든 사용자에게 허용
                        .permitAll()
                )
                
                // 로그아웃 설정
                .logout(logout -> logout
                        // 로그아웃 요청을 처리할 URL
                        .logoutUrl("/logout")
                        // 로그아웃 성공 시 리다이렉트할 URL
                        .logoutSuccessUrl("/")
                );

        // 설정된 SecurityFilterChain을 반환
        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈을 생성합니다.
     * BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 암호화합니다.
     * 
     * @return BCryptPasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
// UserController에 회원가입 POST 핸들러 추가
