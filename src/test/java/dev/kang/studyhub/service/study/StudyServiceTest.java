package dev.kang.studyhub.service.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyComment;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * StudyService 클래스의 단위 테스트
 *
 * 테스트 대상:
 * - 스터디 생성, 수정, 삭제
 * - 스터디 목록/상세 조회
 * - 검색, 페이징, 필터링
 */
@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyApplicationService studyApplicationService;

    @Mock
    private StudyCommentService studyCommentService;

    @InjectMocks
    private StudyService studyService;

    private User leader;
    private Study study;

    @BeforeEach
    void setUp() {
        leader = User.builder()
                .id(1L)
                .name("테스트 리더")
                .email("leader@example.com")
                .password("password")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();
        
        study = Study.builder()
                .title("테스트 스터디")
                .description("설명")
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("조건")
                .deadline(LocalDate.now().plusDays(10))
                .build();
    }

    @Test
    @DisplayName("스터디를 생성할 수 있어야 한다")
    void createStudy_Success() {
        // given
        Study savedStudy = Study.builder()
                .title("테스트 스터디")
                .description("설명")
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("조건")
                .deadline(LocalDate.now().plusDays(10))
                .build();
        
        when(studyRepository.save(any(Study.class))).thenReturn(savedStudy);

        // when
        Study result = studyService.createStudy(study);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).save(study);
    }

    @Test
    @DisplayName("스터디를 수정할 수 있어야 한다")
    void updateStudy_Success() {
        // given
        Study updatedStudy = Study.builder()
                .title("수정된 제목")
                .description("수정된 설명")
                .leader(leader)
                .recruitmentLimit(10)
                .requirement("수정된 조건")
                .deadline(LocalDate.now().plusDays(20))
                .build();
        
        when(studyRepository.save(any(Study.class))).thenReturn(updatedStudy);

        // when
        Study result = studyService.updateStudy(study, "수정된 제목", "수정된 설명", 10, "수정된 조건", LocalDate.now().plusDays(20));

        // then
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getRecruitmentLimit()).isEqualTo(10);
        verify(studyRepository, times(1)).save(study);
    }

    @Test
    @DisplayName("스터디를 삭제할 수 있어야 한다")
    void deleteStudy_Success() {
        // given
        when(studyCommentService.getCommentsByStudy(study)).thenReturn(Collections.emptyList());

        // when
        studyService.deleteStudy(study);

        // then
        verify(studyApplicationService, times(1)).deleteAllByStudy(study);
        verify(studyCommentService, times(1)).getCommentsByStudy(study);
        verify(studyRepository, times(1)).delete(study);
    }

    @Test
    @DisplayName("스터디를 삭제할 때 댓글이 있는 경우 댓글도 함께 삭제되어야 한다")
    void deleteStudy_WithComments_Success() {
        // given
        StudyComment comment1 = StudyComment.builder()
                .user(leader)
                .study(study)
                .content("댓글1")
                .build();
        StudyComment comment2 = StudyComment.builder()
                .user(leader)
                .study(study)
                .content("댓글2")
                .build();
        
        // 리플렉션을 통해 id 설정
        try {
            java.lang.reflect.Field idField1 = StudyComment.class.getDeclaredField("id");
            idField1.setAccessible(true);
            idField1.set(comment1, 1L);
            
            java.lang.reflect.Field idField2 = StudyComment.class.getDeclaredField("id");
            idField2.setAccessible(true);
            idField2.set(comment2, 2L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
        
        when(studyCommentService.getCommentsByStudy(study)).thenReturn(Arrays.asList(comment1, comment2));

        // when
        studyService.deleteStudy(study);

        // then
        verify(studyApplicationService, times(1)).deleteAllByStudy(study);
        verify(studyCommentService, times(1)).getCommentsByStudy(study);
        verify(studyCommentService, times(1)).deleteComment(1L, leader);
        verify(studyCommentService, times(1)).deleteComment(2L, leader);
        verify(studyRepository, times(1)).delete(study);
    }

    @Test
    @DisplayName("스터디 ID로 조회할 수 있어야 한다")
    void findById_Success() {
        // given
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        // when
        Optional<Study> result = studyService.findById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("전체 스터디 목록을 조회할 수 있어야 한다")
    void findAll_Success() {
        // given
        List<Study> studies = Arrays.asList(study);
        when(studyRepository.findAllOrderByCreatedAtDesc()).thenReturn(studies);

        // when
        List<Study> result = studyService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findAllOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("전체 스터디 목록을 페이징 조회할 수 있어야 한다")
    void findAll_WithPaging_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Study> page = new PageImpl<>(Arrays.asList(study));
        when(studyRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(page);

        // when
        Page<Study> result = studyService.findAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findAllOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 목록을 조회할 수 있어야 한다")
    void findByLeader_Success() {
        // given
        List<Study> studies = Arrays.asList(study);
        when(studyRepository.findByLeaderOrderByCreatedAtDesc(leader)).thenReturn(studies);

        // when
        List<Study> result = studyService.findByLeader(leader);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findByLeaderOrderByCreatedAtDesc(leader);
    }

    @Test
    @DisplayName("제목 키워드로 스터디를 검색할 수 있어야 한다")
    void searchByTitle_Success() {
        // given
        List<Study> studies = Arrays.asList(study);
        when(studyRepository.findByTitleContainingOrderByCreatedAtDesc("테스트")).thenReturn(studies);

        // when
        List<Study> result = studyService.searchByTitle("테스트");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findByTitleContainingOrderByCreatedAtDesc("테스트");
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 활성 스터디 목록을 조회할 수 있어야 한다")
    void findActiveStudies_Success() {
        // given
        List<Study> studies = Arrays.asList(study);
        when(studyRepository.findActiveStudiesOrderByCreatedAtDesc(any(LocalDate.class))).thenReturn(studies);

        // when
        List<Study> result = studyService.findActiveStudies();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findActiveStudiesOrderByCreatedAtDesc(any(LocalDate.class));
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 스터디 개수를 조회할 수 있어야 한다")
    void countActiveStudies_Success() {
        // given
        when(studyRepository.countActiveStudies(any(LocalDate.class))).thenReturn(3L);

        // when
        long count = studyService.countActiveStudies();

        // then
        assertThat(count).isEqualTo(3L);
        verify(studyRepository, times(1)).countActiveStudies(any(LocalDate.class));
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 개수를 조회할 수 있어야 한다")
    void countByLeader_Success() {
        // given
        when(studyRepository.countByLeader(leader)).thenReturn(2L);

        // when
        long count = studyService.countByLeader(leader);

        // then
        assertThat(count).isEqualTo(2L);
        verify(studyRepository, times(1)).countByLeader(leader);
    }

    @Test
    @DisplayName("최근 스터디 목록을 조회할 수 있어야 한다")
    void findRecentStudies_Success() {
        // given
        List<Study> studies = Arrays.asList(study);
        when(studyRepository.findAllOrderByCreatedAtDesc()).thenReturn(studies);

        // when
        List<Study> result = studyService.findRecentStudies();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 스터디");
        verify(studyRepository, times(1)).findAllOrderByCreatedAtDesc();
    }
} 