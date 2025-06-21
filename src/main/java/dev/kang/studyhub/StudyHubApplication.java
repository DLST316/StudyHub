package dev.kang.studyhub;

import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * StudyHub 애플리케이션의 메인 클래스
 * 
 * Spring Boot 애플리케이션의 시작점입니다.
 * 
 * 주요 기능:
 * - Spring Boot 애플리케이션 실행
 * - 컴포넌트 스캔 및 자동 설정
 * - 테스트 사용자 자동 생성
 */
@SpringBootApplication
@RequiredArgsConstructor
public class StudyHubApplication {

    private final UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(StudyHubApplication.class, args);
    }

    /**
     * 애플리케이션 시작 시 테스트 사용자를 자동으로 생성하는 CommandLineRunner
     */
    @Bean
    public CommandLineRunner createTestUsers() {
        return args -> {
            // 테스트 사용자 1: 강인석
            if (userService.findByEmail("lolvslol66@gmail.com").isEmpty()) {
                UserJoinForm form1 = new UserJoinForm();
                form1.setName("강인석");
                form1.setEmail("lolvslol66@gmail.com");
                form1.setPassword("123456");
                form1.setUniversity("부산대");
                form1.setMajor("컴퓨터 공학과");
                form1.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(form1);
                System.out.println("테스트 사용자 1 생성 완료: 강인석 (lolvslol66@gmail.com)");
            }

            // 테스트 사용자 2: 강인석2
            if (userService.findByEmail("lolvslol66@icloud.com").isEmpty()) {
                UserJoinForm form2 = new UserJoinForm();
                form2.setName("강인석2");
                form2.setEmail("lolvslol66@icloud.com");
                form2.setPassword("123456");
                form2.setUniversity("부산대");
                form2.setMajor("컴퓨터 공학과");
                form2.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(form2);
                System.out.println("테스트 사용자 2 생성 완료: 강인석2 (lolvslol66@icloud.com)");
            }
        };
    }
}
