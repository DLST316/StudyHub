package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 전용 유저 관리 컨트롤러
 * - 유저 목록 조회 (페이지네이션, 검색, 필터링)
 * - 유저 차단/차단 해제
 * - 유저 상세 정보 조회
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 전체 유저 목록 조회 (테스트 및 관리자 페이지용)
     */
    @GetMapping(produces = "application/json; charset=UTF-8")
    public List<User> listUsers() {
        return userService.findAllUsers();
    }

    /**
     * 전체 유저 목록 조회 (페이지네이션)
     */
    @GetMapping("/api")
    public Page<User> listUsersApi(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        
        if (search != null && !search.trim().isEmpty()) {
            // 검색 기능 (이메일 또는 이름으로 검색)
            return userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        } else if ("blocked".equals(status)) {
            // 차단된 사용자만 조회
            return userRepository.findByIsBlockedTrue(pageable);
        } else if ("active".equals(status)) {
            // 활성 사용자만 조회
            return userRepository.findByIsBlockedFalse(pageable);
        } else {
            // 전체 사용자 조회
            return userRepository.findAll(pageable);
        }
    }

    /**
     * 유저 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 유저 차단
     */
    @PostMapping(value = "/{id}/block", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {
        try {
            userService.blockUser(id);
            return ResponseEntity.ok("유저가 차단되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 유저 차단 해제
     */
    @PostMapping(value = "/{id}/unblock", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {
        try {
            userService.unblockUser(id);
            return ResponseEntity.ok("유저 차단이 해제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 