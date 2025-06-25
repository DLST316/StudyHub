package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

/**
 * AdminUserController 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserControllerTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // 테스트용 유저 생성
        testUser = User.builder()
                .name("관리자")
                .email("admin@test.com")
                .password("encoded")
                .university("테스트대")
                .major("컴퓨터")
                .educationStatus(null)
                .role("ADMIN")
                .isBlocked(false)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("유저 목록 조회 API")
    void listUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@test.com")));
    }

    @Test
    @DisplayName("유저 차단 API - 성공")
    void blockUser_success() throws Exception {
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/block"))
                .andExpect(status().isOk())
                .andExpect(content().string("유저가 차단되었습니다."));
        User blocked = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(blocked.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("유저 차단 해제 API - 성공")
    void unblockUser_success() throws Exception {
        // 미리 차단
        testUser.setBlocked(true);
        userRepository.save(testUser);
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(content().string("유저 차단이 해제되었습니다."));
        User unblocked = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(unblocked.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 유저 차단/해제 - 예외")
    void blockUnblockUser_notFound() throws Exception {
        long notExistId = 99999L;
        mockMvc.perform(post("/admin/users/" + notExistId + "/block"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 유저입니다."));
        mockMvc.perform(post("/admin/users/" + notExistId + "/unblock"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 유저입니다."));
    }
} 