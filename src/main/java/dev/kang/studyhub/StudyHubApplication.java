package dev.kang.studyhub;

import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.user.UserService;
import dev.kang.studyhub.web.user.UserJoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
 * - 테스트 사용자 자동 생성 (개발 환경에서만)
 */
@SpringBootApplication
@RequiredArgsConstructor
public class StudyHubApplication {

    private final UserService userService;
    private final BoardRepository boardRepository;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:}")
    private String adminPassword;

    public static void main(String[] args) {
        SpringApplication.run(StudyHubApplication.class, args);
    }

    /**
     * 개발 환경에서만 테스트 사용자와 어드민 계정을 자동으로 생성하는 CommandLineRunner
     */
    @Bean
    public CommandLineRunner createTestUsers() {
        return args -> {
            // 어드민 계정 자동 생성 (개발 환경에서만)
            if (userService.findByUsername(adminUsername).isEmpty()) {
                UserJoinForm adminForm = new UserJoinForm();
                adminForm.setName("관리자");
                adminForm.setUsername(adminUsername);
                adminForm.setEmail("admin@studyhub.com");
                adminForm.setPassword(adminPassword.isEmpty() ? "admin123" : adminPassword);
                adminForm.setUniversity("StudyHub");
                adminForm.setMajor("시스템 관리");
                adminForm.setEducationStatus(EducationStatus.GRADUATED);
                
                userService.join(adminForm);
                
                // 어드민 역할로 변경
                var adminUser = userService.findByUsername(adminUsername).get();
                adminUser.setRole("ADMIN");
                userService.save(adminUser);
                
                System.out.println("어드민 계정 생성 완료: 관리자 (" + adminUsername + " / " + adminForm.getPassword() + ")");
            }

            // 테스트 사용자 1: 강인석
            if (userService.findByUsername("kanginseok").isEmpty()) {
                UserJoinForm user1Form = new UserJoinForm();
                user1Form.setName("강인석");
                user1Form.setUsername("kanginseok");
                user1Form.setEmail("kanginseok@studyhub.com");
                user1Form.setPassword("password123");
                user1Form.setUniversity("서울대학교");
                user1Form.setMajor("컴퓨터공학과");
                user1Form.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(user1Form);
                System.out.println("테스트 사용자 생성 완료: 강인석 (kanginseok / password123)");
            }

            // 테스트 사용자 2: 김지영
            if (userService.findByUsername("kimjiyoung").isEmpty()) {
                UserJoinForm user2Form = new UserJoinForm();
                user2Form.setName("김지영");
                user2Form.setUsername("kimjiyoung");
                user2Form.setEmail("kimjiyoung@studyhub.com");
                user2Form.setPassword("password123");
                user2Form.setUniversity("연세대학교");
                user2Form.setMajor("경영학과");
                user2Form.setEducationStatus(EducationStatus.ENROLLED);
                
                userService.join(user2Form);
                System.out.println("테스트 사용자 생성 완료: 김지영 (kimjiyoung / password123)");
            }

            // 테스트 사용자 3: 박민수
            if (userService.findByUsername("parkminsu").isEmpty()) {
                UserJoinForm user3Form = new UserJoinForm();
                user3Form.setName("박민수");
                user3Form.setUsername("parkminsu");
                user3Form.setEmail("parkminsu@studyhub.com");
                user3Form.setPassword("password123");
                user3Form.setUniversity("고려대학교");
                user3Form.setMajor("전자공학과");
                user3Form.setEducationStatus(EducationStatus.GRADUATED);
                
                userService.join(user3Form);
                System.out.println("테스트 사용자 생성 완료: 박민수 (parkminsu / password123)");
            }

            // 기본 게시판 생성
            if (boardRepository.count() == 0) {
                Board freeBoard = new Board();
                freeBoard.setName("자유게시판");
                freeBoard.setDescription("자유롭게 이야기를 나누는 공간입니다.");
                freeBoard.setCreatedAt(LocalDateTime.now());
                boardRepository.save(freeBoard);
                System.out.println("기본 게시판 생성 완료");
            }
        };
    }
}
