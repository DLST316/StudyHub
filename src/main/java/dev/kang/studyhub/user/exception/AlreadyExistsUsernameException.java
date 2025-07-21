package dev.kang.studyhub.user.exception;

/**
 * 이미 존재하는 사용자명으로 회원가입을 시도할 때 발생하는 예외
 */
public class AlreadyExistsUsernameException extends RuntimeException {
    public AlreadyExistsUsernameException(String message) {
        super(message);
    }
} 