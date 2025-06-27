package dev.kang.studyhub;

import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.BoardRepository;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;

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
    private final BoardRepository boardRepository;

    public static void main(String[] args) {
        SpringApplication.run(StudyHubApplication.class, args);
    }

    /**
     * 애플리케이션 시작 시 테스트 사용자와 어드민 계정을 자동으로 생성하는 CommandLineRunner
     */
    @Bean
    @Profile("!test")
    public CommandLineRunner createTestUsers() {
        return args -> {
            // 어드민 계정 자동 생성
            if (userService.findByUsername("admin").isEmpty()) {
                UserJoinForm adminForm = new UserJoinForm();
                adminForm.setName("관리자");
                adminForm.setUsername("admin");
                adminForm.setEmail("admin@studyhub.com");
                adminForm.setPassword("admin123");
                adminForm.setUniversity("StudyHub");
                adminForm.setMajor("시스템 관리");
                adminForm.setEducationStatus(EducationStatus.GRADUATED);
                
                userService.join(adminForm);
                
                // 어드민 역할로 변경
                var adminUser = userService.findByUsername("admin").get();
                adminUser.setRole("ADMIN");
                userService.save(adminUser);
                
                System.out.println("어드민 계정 생성 완료: 관리자 (admin / admin123)");
            }

            // 테스트 사용자 1: 강인석
            if (userService.findByUsername("kangin").isEmpty()) {
                UserJoinForm form1 = new UserJoinForm();
                form1.setName("강인석");
                form1.setUsername("kangin");
                form1.setEmail("lolvslol66@gmail.com");
                form1.setPassword("123456");
                form1.setUniversity("부산대");
                form1.setMajor("컴퓨터 공학과");
                form1.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(form1);
                System.out.println("테스트 사용자 1 생성 완료: 강인석 (kangin / 123456)");
            }

            // 테스트 사용자 2: 강인석2
            if (userService.findByUsername("kangin2").isEmpty()) {
                UserJoinForm form2 = new UserJoinForm();
                form2.setName("강인석2");
                form2.setUsername("kangin2");
                form2.setEmail("lolvslol66@icloud.com");
                form2.setPassword("123456");
                form2.setUniversity("부산대");
                form2.setMajor("컴퓨터 공학과");
                form2.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(form2);
                System.out.println("테스트 사용자 2 생성 완료: 강인석2 (kangin2 / 123456)");
            }

            // 커뮤니티 Board row 자동 생성
            if (boardRepository.findByName("커뮤니티") == null) {
                Board board = new Board();
                board.setName("커뮤니티");
                board.setDescription("StudyHub 단일 커뮤니티 게시판");
                board.setCreatedAt(LocalDateTime.now());
                boardRepository.save(board);
                System.out.println("커뮤니티 Board row 자동 생성 완료");
            }
        };
    }
}
