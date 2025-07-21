package dev.kang.studyhub.user.service;

import dev.kang.studyhub.user.entity.User;
import dev.kang.studyhub.user.repository.UserRepository;
import dev.kang.studyhub.user.exception.AlreadyExistsEmailException;
import dev.kang.studyhub.user.exception.AlreadyExistsUsernameException;
import dev.kang.studyhub.user.dto.UserJoinForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * 주요 기능:
 * - 회원가입 처리
 * - 사용자 정보 조회 (사용자명, 이메일)
 * - 비밀번호 암호화
 * 
 * 컨트롤러와 리포지토리 사이의 중간 계층으로,
 * 비즈니스 규칙과 데이터 처리 로직을 담당합니다.
 */
@RequiredArgsConstructor
@Service
public class UserService {

    // 사용자 데이터 접근을 위한 리포지토리
    private final UserRepository userRepository;
    
    // 비밀번호 암호화를 위한 인코더
    private final PasswordEncoder passwordEncoder;

    /**
     * 새로운 사용자 회원가입을 처리하는 메서드
     * 
     * 처리 과정:
     * 1. 사용자명 중복 검사
     * 2. 이메일 중복 검사 (이메일이 제공된 경우)
     * 3. 비밀번호 암호화
     * 4. 사용자 엔티티 생성
     * 5. 데이터베이스 저장
     * 
     * @param form 사용자가 입력한 회원가입 정보
     * @throws AlreadyExistsUsernameException 이미 존재하는 사용자명인 경우
     * @throws AlreadyExistsEmailException 이미 존재하는 이메일인 경우
     */
    public void join(UserJoinForm form) {
        // 사용자명 중복 검사
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new AlreadyExistsUsernameException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 검사 (이메일이 제공된 경우에만)
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(form.getEmail())) {
                throw new AlreadyExistsEmailException("이미 사용 중인 이메일입니다.");
            }
        }

        // 비밀번호를 BCrypt로 암호화
        // 평문 비밀번호를 안전한 해시값으로 변환
        String encodedPassword = passwordEncoder.encode(form.getPassword());
    
        // User 엔티티를 빌더 패턴으로 생성
        User user = User.builder()
                .name(form.getName())                    // 사용자 이름
                .username(form.getUsername())            // 사용자명 (로그인 ID)
                .email(form.getEmail())                  // 이메일 (연락처용, 선택사항)
                .password(encodedPassword)               // 암호화된 비밀번호
                .university(form.getUniversity())        // 대학교
                .major(form.getMajor())                  // 전공
                .educationStatus(form.getEducationStatus()) // 학력 상태
                .role("USER")                            // 기본 역할 (USER)
                .isBlocked(false)                         // 차단 여부(기본값 false)
                .build();
    
        // 생성된 사용자 정보를 데이터베이스에 저장
        userRepository.save(user);
    }
    
    /**
     * 사용자명으로 사용자를 조회하는 메서드 (로그인용)
     * 
     * @param username 조회할 사용자의 사용자명
     * @return 사용자 정보 (Optional로 감싸져 있음)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 이메일로 사용자를 조회하는 메서드 (연락처용)
     * 
     * @param email 조회할 사용자의 이메일
     * @return 사용자 정보 (Optional로 감싸져 있음)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 정보를 저장/수정하는 메서드
     * 
     * @param user 저장할 사용자 엔티티
     */
    public void save(User user) {
        userRepository.save(user);
    }

    /**
     * 관리자: 모든 사용자 목록 조회
     * @return 모든 사용자 목록
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 관리자: 유저 차단
     * @param userId 차단할 유저의 ID
     * @return 차단 완료 메시지
     * @throws IllegalArgumentException 유저가 존재하지 않을 때
     * @throws IllegalStateException 관리자 계정을 차단하려고 할 때
     */
    public String blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        
        // 관리자 계정은 차단할 수 없음
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("관리자 계정은 차단할 수 없습니다.");
        }
        
        user.setBlocked(true);
        userRepository.save(user);
        return "유저가 차단되었습니다.";
    }

    /**
     * 관리자: 유저 차단 해제
     * @param userId 차단 해제할 유저의 ID
     * @return 차단 해제 완료 메시지
     * @throws IllegalArgumentException 유저가 존재하지 않을 때
     */
    public String unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        
        user.setBlocked(false);
        userRepository.save(user);
        return "유저 차단이 해제되었습니다.";
    }
}