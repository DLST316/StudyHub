package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 관리자 사용자 관리 컨트롤러 통합 테스트
 * 실제 Spring Boot 환경과 DB를 사용하여 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminUserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private User adminUser;
    private User regularUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 기존 데이터 정리
        userRepository.deleteAll();

        // 테스트 데이터 준비
        adminUser = createAdminUser();
        regularUser = createRegularUser();
        blockedUser = createBlockedUser();

        // DB에 저장
        userRepository.save(adminUser);
        userRepository.save(regularUser);
        userRepository.save(blockedUser);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 목록 페이지 접근 - 어드민 권한")
    void usersPage_AdminAccess() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("사용자 목록 페이지 접근 - 일반 사용자 권한")
    void usersPage_UserAccess() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 목록 페이지 접근 - 로그인하지 않은 사용자")
    void usersPage_NotLoggedIn() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("전체 사용자 목록 조회 API")
    void listUsers_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        // 비즈니스 로직 검증
        List<User> users = userService.findAllUsers();
        assertThat(users).hasSize(3); // adminUser, regularUser, blockedUser
        assertThat(users).extracting("email")
                .contains("admin@test.com", "user@test.com", "blocked@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 목록 조회 - 페이지네이션")
    void listUsersApi_WithPaging() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        Page<User> userPage = userRepository.findAll(PageRequest.of(0, 20));
        assertThat(userPage.getContent()).hasSize(3);
        assertThat(userPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 검색 - 이메일로 검색")
    void searchUsers_ByEmail() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api")
                        .param("search", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        Page<User> searchResult = userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                "user@test.com", "user@test.com", PageRequest.of(0, 20));
        assertThat(searchResult.getContent()).hasSize(1);
        assertThat(searchResult.getContent().get(0).getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 검색 - 이름으로 검색")
    void searchUsers_ByName() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api")
                        .param("search", "일반"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        Page<User> searchResult = userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                "일반", "일반", PageRequest.of(0, 20));
        assertThat(searchResult.getContent()).hasSize(1);
        assertThat(searchResult.getContent().get(0).getName()).isEqualTo("일반 사용자");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("차단된 사용자 목록 조회")
    void listBlockedUsers() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api")
                        .param("status", "blocked"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        Page<User> blockedUsers = userRepository.findByIsBlockedTrue(PageRequest.of(0, 20));
        assertThat(blockedUsers.getContent()).hasSize(1);
        assertThat(blockedUsers.getContent().get(0).getEmail()).isEqualTo("blocked@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("활성 사용자 목록 조회")
    void listActiveUsers() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api")
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        Page<User> activeUsers = userRepository.findByIsBlockedFalse(PageRequest.of(0, 20));
        assertThat(activeUsers.getContent()).hasSize(2); // adminUser, regularUser
        assertThat(activeUsers.getContent()).extracting("email")
                .contains("admin@test.com", "user@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 상세 조회 - 존재하는 사용자")
    void getUser_ExistingUser() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api/" + regularUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        User foundUser = userRepository.findById(regularUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 상세 조회 - 존재하지 않는 사용자")
    void getUser_NonExistingUser() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 차단 - 성공")
    void blockUser_Success() throws Exception {
        // given - 초기 상태 확인
        assertThat(regularUser.isBlocked()).isFalse();

        // when & then
        mockMvc.perform(post("/admin/users/api/" + regularUser.getId() + "/block"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 비즈니스 로직 검증
        User updatedUser = userRepository.findById(regularUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.isBlocked()).isTrue();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 차단 - 존재하지 않는 사용자")
    void blockUser_NonExistingUser() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/users/api/999/block"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 차단 - 관리자 계정 차단 시도")
    void blockUser_AdminUser() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/users/api/" + adminUser.getId() + "/block"))
                .andExpect(status().isBadRequest());

        // 비즈니스 로직 검증 - 관리자는 차단되지 않았는지 확인
        User adminUserAfterAttempt = userRepository.findById(adminUser.getId()).orElse(null);
        assertThat(adminUserAfterAttempt).isNotNull();
        assertThat(adminUserAfterAttempt.isBlocked()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 차단 해제 - 성공")
    void unblockUser_Success() throws Exception {
        // given - 차단된 사용자 상태 확인
        assertThat(blockedUser.isBlocked()).isTrue();

        // when & then
        mockMvc.perform(post("/admin/users/api/" + blockedUser.getId() + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));

        // 비즈니스 로직 검증
        User updatedUser = userRepository.findById(blockedUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.isBlocked()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("사용자 차단 해제 - 존재하지 않는 사용자")
    void unblockUser_NonExistingUser() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/users/api/999/unblock"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("사용자 관리 API 접근 - 일반 사용자 권한")
    void userManagementApi_UserAccess() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/users/api"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users/api/1/block"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users/api/1/unblock"))
                .andExpect(status().isForbidden());
    }

    /**
     * 테스트용 어드민 사용자 생성
     */
    private User createAdminUser() {
        return User.builder()
                .name("관리자")
                .username("admin")
                .email("admin@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("ADMIN")
                .isBlocked(false)
                .build();
    }

    /**
     * 테스트용 일반 사용자 생성
     */
    private User createRegularUser() {
        return User.builder()
                .name("일반 사용자")
                .username("user")
                .email("user@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("USER")
                .isBlocked(false)
                .build();
    }

    /**
     * 테스트용 차단된 사용자 생성
     */
    private User createBlockedUser() {
        return User.builder()
                .name("차단된 사용자")
                .username("blocked")
                .email("blocked@test.com")
                .password("password")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(dev.kang.studyhub.domain.user.model.EducationStatus.ENROLLED)
                .role("USER")
                .isBlocked(true)
                .build();
    }
} 