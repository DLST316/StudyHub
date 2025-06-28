package dev.kang.studyhub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
 * - CSRF 보호 및 보안 헤더 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    /**
     * 개발 환경용 Spring Security 설정
     * 개발 편의를 위해 일부 보안 기능을 완화합니다.
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    @Profile("h2")  // H2 프로파일 (개발 환경)
    public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 개발 환경에서는 CSRF 비활성화 (H2 콘솔 사용을 위해)
                .csrf(AbstractHttpConfigurer::disable)
                
                // H2 콘솔의 iframe 사용을 허용
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
                        .requestMatchers("/api/images/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/?success=login")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    /**
     * 운영 환경용 Spring Security 설정
     * 강화된 보안 설정을 적용합니다.
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    @Profile("prod")  // 운영 환경 프로파일
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/images/upload"))
                
                // 보안 헤더 설정
                .headers(headers -> headers
                        // Clickjacking 방지
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // XSS 방지
                        .xssProtection(xss -> xss.disable())
                        // Content Type Sniffing 방지
                        .contentTypeOptions(contentType -> contentType.disable())
                        // HSTS 헤더 설정
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                        )
                        // Content Security Policy 설정
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'")
                        )
                )
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/images/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/?success=login")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    /**
     * 기본 Spring Security 설정 (프로파일이 지정되지 않은 경우)
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    @Profile("!h2 && !prod")  // h2, prod 프로파일이 아닌 경우
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/images/upload"))
                
                // 기본 보안 헤더 설정
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(xss -> xss.disable())
                        .contentTypeOptions(contentType -> contentType.disable())
                )
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/join", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/images/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/?success=login")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

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
