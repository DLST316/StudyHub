package dev.kang.studyhub.service.study;

import dev.kang.studyhub.study.entity.Study;
import dev.kang.studyhub.study.entity.StudyApplication;
import dev.kang.studyhub.study.model.ApplicationStatus;
import dev.kang.studyhub.study.repository.StudyApplicationRepository;
import dev.kang.studyhub.study.service.StudyApplicationService;
import dev.kang.studyhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * StudyApplicationService 클래스의 단위 테스트
 *
 * 테스트 대상:
 * - 스터디 신청/취소/승인/거절/중복방지
 * - 신청 내역/상태 조회
 */
class StudyApplicationServiceTest {

    @Mock
    private StudyApplicationRepository studyApplicationRepository;

    @InjectMocks
    private StudyApplicationService studyApplicationService;

    private User user;
    private Study study;
    private StudyApplication application;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .name("신청자")
                .username("study_app_service_user")
                .email("user@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        
        // Reflection을 사용하여 id 설정
        // TODO: 오버엔지니어링 아닌가?
        java.lang.reflect.Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);
        
        study = Study.builder()
                .title("테스트 스터디")
                .description("설명")
                .leader(user)
                .recruitmentLimit(5)
                .requirement("조건")
                .build();
        
        // Reflection을 사용하여 study의 id 설정
        java.lang.reflect.Field studyIdField = Study.class.getDeclaredField("id");
        studyIdField.setAccessible(true);
        studyIdField.set(study, 1L);
        
        application = StudyApplication.builder()
                .user(user)
                .study(study)
                .status(ApplicationStatus.PENDING)
                .build();
        
        // Reflection을 사용하여 application의 id 설정
        java.lang.reflect.Field appIdField = StudyApplication.class.getDeclaredField("id");
        appIdField.setAccessible(true);
        appIdField.set(application, 1L);
    }

    @Test
    @DisplayName("스터디를 신청할 수 있어야 한다")
    void apply_Success() {
        // given
        when(studyApplicationRepository.existsByUserAndStudy(user, study)).thenReturn(false);
        when(studyApplicationRepository.save(any(StudyApplication.class))).thenReturn(application);

        // when
        StudyApplication result = studyApplicationService.apply(user, study);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        verify(studyApplicationRepository, times(1)).save(any(StudyApplication.class));
    }

    @Test
    @DisplayName("이미 신청한 스터디는 중복 신청이 불가하다")
    void apply_Duplicate_ThrowsException() {
        // given
        when(studyApplicationRepository.existsByUserAndStudy(user, study)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> studyApplicationService.apply(user, study))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 신청한 스터디입니다.");
    }

    @Test
    @DisplayName("신청을 취소할 수 있어야 한다")
    void cancel_Success() {
        // given
        when(studyApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        // when
        studyApplicationService.cancel(1L, user);

        // then
        verify(studyApplicationRepository, times(1)).delete(application);
    }

    @Test
    @DisplayName("본인이 아닌 사용자는 신청을 취소할 수 없다")
    void cancel_NotOwner_ThrowsException() {
        // given
        User otherUser = User.builder()
                .id(2L)
                .name("다른 유저")
                .username("study_app_service_other")
                .email("other@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        when(studyApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() -> studyApplicationService.cancel(1L, otherUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("본인만 신청을 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("신청을 승인할 수 있어야 한다")
    void approve_Success() {
        // given
        when(studyApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(studyApplicationRepository.save(any(StudyApplication.class))).thenReturn(application);

        // when
        studyApplicationService.approve(1L);

        // then
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(studyApplicationRepository, times(1)).save(application);
    }

    @Test
    @DisplayName("신청을 거절할 수 있어야 한다")
    void reject_Success() {
        // given
        when(studyApplicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(studyApplicationRepository.save(any(StudyApplication.class))).thenReturn(application);

        // when
        studyApplicationService.reject(1L);

        // then
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(studyApplicationRepository, times(1)).save(application);
    }

    @Test
    @DisplayName("특정 사용자의 신청 내역을 조회할 수 있어야 한다")
    void findByUser_Success() {
        // given
        when(studyApplicationRepository.findByUserWithStudyAndLeader(user)).thenReturn(Arrays.asList(application));

        // when
        List<StudyApplication> result = studyApplicationService.findByUser(user);

        // then
        assertThat(result).hasSize(1);
        verify(studyApplicationRepository, times(1)).findByUserWithStudyAndLeader(user);
    }

    @Test
    @DisplayName("특정 스터디의 신청 내역을 조회할 수 있어야 한다")
    void findByStudy_Success() {
        // given
        when(studyApplicationRepository.findByStudyOrderByAppliedAtDesc(study)).thenReturn(Arrays.asList(application));

        // when
        List<StudyApplication> result = studyApplicationService.findByStudy(study);

        // then
        assertThat(result).hasSize(1);
        verify(studyApplicationRepository, times(1)).findByStudyOrderByAppliedAtDesc(study);
    }

    @Test
    @DisplayName("특정 스터디의 승인된 신청 개수를 조회할 수 있어야 한다")
    void countApprovedApplications_Success() {
        // given
        when(studyApplicationRepository.countByStudyAndStatus(study, ApplicationStatus.APPROVED)).thenReturn(2L);

        // when
        long count = studyApplicationService.countApprovedApplications(study);

        // then
        assertThat(count).isEqualTo(2L);
        verify(studyApplicationRepository, times(1)).countByStudyAndStatus(study, ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("특정 스터디의 대기중인 신청 개수를 조회할 수 있어야 한다")
    void countPendingApplications_Success() {
        // given
        when(studyApplicationRepository.countByStudyAndStatus(study, ApplicationStatus.PENDING)).thenReturn(1L);

        // when
        long count = studyApplicationService.countPendingApplications(study);

        // then
        assertThat(count).isEqualTo(1L);
        verify(studyApplicationRepository, times(1)).countByStudyAndStatus(study, ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 이미 신청했는지 확인할 수 있어야 한다")
    void hasApplied_Success() {
        // given
        when(studyApplicationRepository.existsByUserAndStudy(user, study)).thenReturn(true);

        // when
        boolean result = studyApplicationService.hasApplied(user, study);

        // then
        assertThat(result).isTrue();
        verify(studyApplicationRepository, times(1)).existsByUserAndStudy(user, study);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 승인된 신청이 있는지 확인할 수 있어야 한다")
    void isApproved_Success() {
        // given
        when(studyApplicationRepository.existsByUserAndStudyAndStatus(user, study, ApplicationStatus.APPROVED)).thenReturn(true);

        // when
        boolean result = studyApplicationService.isApproved(user, study);

        // then
        assertThat(result).isTrue();
        verify(studyApplicationRepository, times(1)).existsByUserAndStudyAndStatus(user, study, ApplicationStatus.APPROVED);
    }
} 