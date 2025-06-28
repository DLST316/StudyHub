package dev.kang.studyhub.web.admin;

import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.study.entity.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.Collections;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminPageController 통합테스트
 * - 각 관리자 페이지 엔드포인트가 정상적으로 동작하는지 검증
 * - @WithMockUser(roles = "ADMIN")로 인증된 ADMIN 권한 사용자로 테스트
 */
@WithMockUser(roles = "ADMIN")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminPageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostRepository postRepository;
    @MockBean
    private PostCommentRepository commentRepository;
    @MockBean
    private StudyRepository studyRepository;

    @Test
    @DisplayName("대시보드 페이지 정상 반환")
    void dashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @DisplayName("게시글 관리 페이지 정상 반환")
    void posts() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Post> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(postRepository.findAll(pageable)).willReturn(page);
        mockMvc.perform(get("/admin/posts").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/posts"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @DisplayName("댓글 관리 페이지 정상 반환")
    void comments() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<PostComment> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(commentRepository.findAll(pageable)).willReturn(page);
        mockMvc.perform(get("/admin/comments").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/comments"))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    @DisplayName("스터디 관리 페이지 정상 반환")
    void studies() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Study> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        given(studyRepository.findAll(pageable)).willReturn(page);
        mockMvc.perform(get("/admin/studies").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/studies"))
                .andExpect(model().attributeExists("studies"));
    }

    @Test
    @DisplayName("시스템 설정 페이지 정상 반환")
    void settings() throws Exception {
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/settings"));
    }
} 