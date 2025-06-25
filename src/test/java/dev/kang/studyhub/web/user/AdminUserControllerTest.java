package dev.kang.studyhub.web.user;

import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminUserController 단위 테스트
 * 
 * 테스트 대상:
 * - 관리자 사용자 관리 API 엔드포인트
 * - 사용자 목록 조회
 * - 사용자 차단/해제 기능
 * 
 * 테스트 시나리오:
 * - 정상적인 API 호출
 * - 존재하지 않는 사용자 처리
 * - 권한 검증
 */
@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private AdminUserController adminUserController;
    
    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();
        
        // 테스트용 유저 생성
        testUser = User.builder()
                .id(1L)
                .name("관리자")
                .email("admin@test.com")
                .password("encoded")
                .university("테스트대")
                .major("컴퓨터")
                .educationStatus(null)
                .role("ADMIN")
                .isBlocked(false)
                .build();
    }

    @Test
    @DisplayName("사용자 목록 조회 API - 성공")
    void listUsers_Success() throws Exception {
        // given
        when(userService.findAllUsers()).thenReturn(java.util.List.of(testUser));
        
        // when & then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@test.com")));
        
        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("사용자 차단 API - 성공")
    void blockUser_Success() throws Exception {
        // given
        when(userService.blockUser(anyLong())).thenReturn("유저가 차단되었습니다.");
        
        // when & then
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/block"))
                .andExpect(status().isOk())
                .andExpect(content().string("유저가 차단되었습니다."));
        
        verify(userService, times(1)).blockUser(testUser.getId());
    }

    @Test
    @DisplayName("사용자 차단 해제 API - 성공")
    void unblockUser_Success() throws Exception {
        // given
        when(userService.unblockUser(anyLong())).thenReturn("유저 차단이 해제되었습니다.");
        
        // when & then
        mockMvc.perform(post("/admin/users/" + testUser.getId() + "/unblock"))
                .andExpect(status().isOk())
                .andExpect(content().string("유저 차단이 해제되었습니다."));
        
        verify(userService, times(1)).unblockUser(testUser.getId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 차단 시 예외 처리")
    void blockUser_NotFound_ThrowsException() throws Exception {
        // given
        long notExistId = 99999L;
        when(userService.blockUser(notExistId)).thenThrow(new IllegalArgumentException("존재하지 않는 유저입니다."));
        
        // when & then
        mockMvc.perform(post("/admin/users/" + notExistId + "/block"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 유저입니다."));
        
        verify(userService, times(1)).blockUser(notExistId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 차단 해제 시 예외 처리")
    void unblockUser_NotFound_ThrowsException() throws Exception {
        // given
        long notExistId = 99999L;
        when(userService.unblockUser(notExistId)).thenThrow(new IllegalArgumentException("존재하지 않는 유저입니다."));
        
        // when & then
        mockMvc.perform(post("/admin/users/" + notExistId + "/unblock"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 유저입니다."));
        
        verify(userService, times(1)).unblockUser(notExistId);
    }
} 