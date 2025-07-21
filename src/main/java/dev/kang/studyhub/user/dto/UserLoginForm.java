package dev.kang.studyhub.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 폼 데이터를 담는 DTO(Data Transfer Object) 클래스
 * 
 * 현재는 홈페이지에서 직접 Spring Security의 기본 로그인 폼을 사용하므로
 * 이 클래스는 참고용으로 남겨두었습니다.
 * 
 * 향후 별도의 로그인 페이지를 만들거나 로그인 관련 추가 기능이 필요할 때 사용할 수 있습니다.
 */
@Getter
@Setter
public class UserLoginForm {

    /**
     * 사용자 아이디 (로그인용)
     * 필수 입력 항목입니다.
     */
    @NotBlank(message = "아이디를 입력하세요.")
    private String username;

    /**
     * 사용자 비밀번호
     * 필수 입력 항목입니다.
     */
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;
}