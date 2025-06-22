package dev.kang.studyhub.web.study;

import dev.kang.studyhub.config.TestSecurityConfig;
import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import dev.kang.studyhub.service.study.StudyApplicationService;
import dev.kang.studyhub.service.study.StudyCommentService;
import dev.kang.studyhub.service.study.StudyService;
import dev.kang.studyhub.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudyController 클래스의 웹 계층 테스트
 * 
 * 테스트 대상:
 * - 스터디 목록 조회 (GET /studies)
 * - 스터디 상세 조회 (GET /studies/{id})
 * - 스터디 생성 폼/처리 (GET/POST /studies/new)
 * - 스터디 수정 폼/처리 (GET/POST /studies/edit/{id})
 * - 스터디 삭제 처리 (POST /studies/delete/{id})
 * - 내가 개설한 스터디 목록 (GET /studies/my)
 * - 키워드 검색 (GET /studies?keyword=...)
 * 
 * 테스트 시나리오:
 * - 정상적인 스터디 CRUD 작업
 * - 권한 검증 (스터디 개설자만 수정/삭제 가능)
 * - 예외 상황 처리 (존재하지 않는 스터디, 권한 없음)
 * - 페이징 및 검색 기능
 */
@WebMvcTest(StudyController.class)
@Import(TestSecurityConfig.class)
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyService studyService;

    @MockBean
    private UserService userService;

    @MockBean
    private StudyApplicationService studyApplicationService;

    @MockBean
    private StudyCommentService studyCommentService;

    private User testUser;
    private Study testStudy;

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     * 
     * 설정 내용:
     * - 테스트용 사용자 엔티티 생성
     * - 테스트용 스터디 엔티티 생성
     * - UserService Mock 설정 (이메일로 사용자 조회)
     * - StudyApplicationService Mock 설정 (신청 여부 확인)
     * - StudyCommentService Mock 설정 (댓글 목록 조회 시 빈 리스트 반환)
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 엔티티 생성
        testUser = User.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@example.com")
                .password("encodedPassword")
                .university("테스트 대학교")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        // 테스트용 스터디 엔티티 생성
        testStudy = Study.builder()
                .title("테스트 스터디")
                .description("테스트 스터디 설명")
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();

        // UserService Mock 설정 - 이메일로 사용자 조회 시 테스트 사용자 반환
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // StudyApplicationService Mock 설정 - 신청 여부 확인 시 기본적으로 false 반환
        when(studyApplicationService.hasApplied(any(User.class), any(Study.class))).thenReturn(false);
        
        // StudyCommentService Mock 설정 - 댓글 목록 조회 시 빈 리스트 반환
        when(studyCommentService.getCommentsByStudy(any(Study.class))).thenReturn(List.of());
    }

    @Test
    @DisplayName("스터디 목록 페이지 조회")
    @WithMockUser
    void listStudies() throws Exception {
        // given
        // 페이징된 스터디 목록 Mock 설정
        Page<Study> studyPage = new PageImpl<>(List.of(testStudy));
        when(studyService.findAll(any(Pageable.class))).thenReturn(studyPage);

        // when & then
        // 스터디 목록 페이지 요청 및 응답 검증
        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())                    // HTTP 200 상태 코드
                .andExpect(view().name("study/list"))          // study/list 뷰 반환
                .andExpect(model().attributeExists("studies")) // studies 모델 속성 존재
                .andExpect(model().attributeExists("currentPage")) // currentPage 모델 속성 존재
                .andExpect(model().attributeExists("totalPages")); // totalPages 모델 속성 존재
    }

    @Test
    @DisplayName("스터디 상세 페이지 조회")
    @WithMockUser
    void viewStudy() throws Exception {
        // given
        // 특정 ID의 스터디 조회 시 테스트 스터디 반환하도록 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        // 스터디 상세 페이지 요청 및 응답 검증
        mockMvc.perform(get("/studies/1"))
                .andExpect(status().isOk())           // HTTP 200 상태 코드
                .andExpect(view().name("study/detail")) // study/detail 뷰 반환
                .andExpect(model().attributeExists("study")); // study 모델 속성 존재
    }

    @Test
    @DisplayName("존재하지 않는 스터디 조회 시 예외 발생")
    @WithMockUser
    void viewStudyNotFound() throws Exception {
        // given
        when(studyService.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/studies/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "스터디를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("스터디 생성 폼 페이지 조회")
    @WithMockUser
    void showCreateForm() throws Exception {
        // when & then
        // 스터디 생성 폼 페이지 요청 및 응답 검증
        mockMvc.perform(get("/studies/new"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/form"))    // study/form 뷰 반환
                .andExpect(model().attributeExists("studyForm")); // studyForm 모델 속성 존재
    }

    @Test
    @DisplayName("스터디 생성 처리")
    @WithMockUser(username = "test@example.com")
    void createStudy() throws Exception {
        // given
        // 생성될 스터디 엔티티 정의
        Study savedStudy = Study.builder()
                .title("새로운 스터디")
                .description("새로운 스터디 설명")
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("Java 기초")
                .deadline(LocalDate.of(2024, 12, 31))
                .build();
        
        // Mock을 통해 ID가 설정된 Study를 반환하도록 설정
        // 리플렉션을 사용하여 엔티티의 ID 필드에 값을 설정
        when(studyService.createStudy(any(Study.class))).thenAnswer(invocation -> {
            Study study = invocation.getArgument(0);
            // 리플렉션을 사용하여 ID 설정
            try {
                java.lang.reflect.Field idField = Study.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(study, 1L);
            } catch (Exception e) {
                // 리플렉션 실패 시 무시
            }
            return study;
        });

        // when & then
        // 스터디 생성 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/new")
                        .param("title", "새로운 스터디")
                        .param("description", "새로운 스터디 설명")
                        .param("recruitmentLimit", "5")
                        .param("requirement", "Java 기초")
                        .param("deadline", "2024-12-31"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1")); // 생성된 스터디 상세 페이지로 리다이렉트
    }

    @Test
    @DisplayName("스터디 수정 폼 페이지 조회")
    @WithMockUser(username = "test@example.com")
    void showEditForm() throws Exception {
        // given
        // 수정할 스터디 조회 시 테스트 스터디 반환하도록 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(testStudy));

        // when & then
        // 스터디 수정 폼 페이지 요청 및 응답 검증
        mockMvc.perform(get("/studies/edit/1"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/form"))    // study/form 뷰 반환
                .andExpect(model().attributeExists("studyForm")) // studyForm 모델 속성 존재
                .andExpect(model().attributeExists("studyId"));  // studyId 모델 속성 존재
    }

    @Test
    @DisplayName("내가 개설한 스터디 목록 조회")
    @WithMockUser(username = "test@example.com")
    void myStudies() throws Exception {
        // given
        // 현재 사용자가 개설한 스터디 목록 반환하도록 Mock 설정
        when(studyService.findByLeader(any(User.class))).thenReturn(List.of(testStudy));

        // when & then
        // 내가 개설한 스터디 목록 요청 및 응답 검증
        mockMvc.perform(get("/studies/my"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/my-studies")) // study/my-studies 뷰 반환
                .andExpect(model().attributeExists("studies")); // studies 모델 속성 존재
    }

    @Test
    @DisplayName("스터디 수정 처리")
    @WithMockUser(username = "test@example.com")
    void updateStudy() throws Exception {
        // given
        // 수정할 스터디 엔티티 생성 (ID 포함)
        Study existingStudy = Study.builder()
                .title("기존 스터디")
                .description("기존 스터디 설명")
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("기존 조건")
                .deadline(LocalDate.now().plusDays(10))
                .build();
        
        // ID 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field idField = Study.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingStudy, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
        
        // 스터디 조회 및 수정 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(existingStudy));
        when(studyService.updateStudy(any(Study.class), anyString(), anyString(), any(), anyString(), any(LocalDate.class)))
                .thenReturn(existingStudy);

        // when & then
        // 스터디 수정 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/edit/1")
                        .param("title", "수정된 스터디")
                        .param("description", "수정된 스터디 설명")
                        .param("recruitmentLimit", "10")
                        .param("requirement", "수정된 조건")
                        .param("deadline", "2024-12-31"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies/1")); // 수정된 스터디 상세 페이지로 리다이렉트
    }

    @Test
    @DisplayName("스터디 삭제 처리")
    @WithMockUser(username = "test@example.com")
    void deleteStudy() throws Exception {
        // given
        // 삭제할 스터디 엔티티 생성 (ID 포함)
        Study existingStudy = Study.builder()
                .title("삭제할 스터디")
                .description("삭제할 스터디 설명")
                .leader(testUser)
                .recruitmentLimit(5)
                .requirement("삭제할 조건")
                .deadline(LocalDate.now().plusDays(10))
                .build();
        
        // ID 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field idField = Study.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingStudy, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
        
        // 스터디 조회 Mock 설정
        when(studyService.findById(1L)).thenReturn(Optional.of(existingStudy));

        // when & then
        // 스터디 삭제 POST 요청 및 응답 검증
        mockMvc.perform(post("/studies/delete/1"))
                .andExpect(status().is3xxRedirection())  // HTTP 302 리다이렉트
                .andExpect(redirectedUrl("/studies"));   // 스터디 목록 페이지로 리다이렉트
    }

    @Test
    @DisplayName("키워드로 스터디 검색")
    @WithMockUser
    void searchStudies() throws Exception {
        // given
        // 키워드 검색 결과 반환하도록 Mock 설정
        when(studyService.searchByTitle("Java")).thenReturn(List.of(testStudy));

        // when & then
        // 키워드 검색 요청 및 응답 검증
        mockMvc.perform(get("/studies").param("keyword", "Java"))
                .andExpect(status().isOk())              // HTTP 200 상태 코드
                .andExpect(view().name("study/list"))    // study/list 뷰 반환
                .andExpect(model().attributeExists("studies")) // studies 모델 속성 존재
                .andExpect(model().attribute("keyword", "Java")); // keyword 모델 속성 값 검증
    }
} 