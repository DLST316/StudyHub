package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.model.EducationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 폼 데이터를 담는 DTO(Data Transfer Object) 클래스
 * 
 * 이 클래스는 웹 폼에서 전송되는 데이터를 받아서 서비스 계층으로 전달하는 역할을 합니다.
 * 
 * 주요 특징:
 * - 폼 데이터 바인딩: Thymeleaf 폼과 자동으로 바인딩됨
 * - 유효성 검사: Bean Validation 어노테이션으로 입력값 검증
 * - 계층 간 데이터 전달: 컨트롤러 ↔ 서비스 간 데이터 전달
 */
@Getter
@Setter
public class UserJoinForm {

    /**
     * 사용자 이름
     * 필수 입력 항목이며, 공백이 아닌 문자열이어야 합니다.
     */
    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    /**
     * 사용자 아이디 (로그인용)
     * 필수 입력 항목이며, 영문자, 숫자, 언더스코어만 허용합니다.
     */
    @NotBlank(message = "아이디를 입력하세요.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문자, 숫자, 언더스코어만 사용할 수 있습니다.")
    private String username;

    /**
     * 사용자 이메일 (연락처용)
     * 선택 입력 항목이며, 올바른 이메일 형식이어야 합니다.
     */
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    /**
     * 사용자 비밀번호
     * 필수 입력 항목이며, 최소 6자 이상이어야 합니다.
     */
    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    private String password;

    /**
     * 대학교명
     * 선택 입력 항목입니다.
     */
    private String university;

    /**
     * 전공명
     * 선택 입력 항목입니다.
     */
    private String major;

    /**
     * 학력 상태 (재학, 졸업, 휴학 등)
     * 필수 선택 항목입니다.
     */
    @NotNull(message = "학력 상태를 선택하세요.")
    private EducationStatus educationStatus;
}