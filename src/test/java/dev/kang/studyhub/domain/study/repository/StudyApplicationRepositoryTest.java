package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyApplicationRepository 통합 테스트
 *
 * - 스터디 신청 CRUD 작업
 * - 신청 상태별 조회
 * - 사용자별/스터디별 신청 조회
 * - 중복 신청 방지
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StudyApplicationRepositoryTest {
    @Autowired
    private StudyApplicationRepository studyApplicationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User leader;
    private Study study1;
    private Study study2;
    private StudyApplication application1;
    private StudyApplication application2;
    private StudyApplication application3;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자들 생성
        user1 = User.builder()
                .name("신청자1")
                .username("study_app_user1")
                .email("user1@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .name("신청자2")
                .username("study_app_user2")
                .email("user2@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(user2);

        leader = User.builder()
                .name("스터디장")
                .username("study_app_leader")
                .email("leader@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(leader);

        // 테스트용 스터디 생성
        study1 = Study.builder()
                .title("Java 스터디")
                .description("Java 기초부터 심화까지")
                .leader(leader)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();
        studyRepository.save(study1);

        study2 = Study.builder()
                .title("Spring Boot 스터디")
                .description("Spring Boot 실전 프로젝트")
                .leader(leader)
                .recruitmentLimit(3)
                .requirement("Java, Spring 기초")
                .deadline(LocalDate.now().plusDays(14))
                .build();
        studyRepository.save(study2);

        // 테스트용 신청 생성
        application1 = StudyApplication.builder()
                .user(user1)
                .study(study1)
                .status(ApplicationStatus.PENDING)
                .build();
        studyApplicationRepository.save(application1);

        application2 = StudyApplication.builder()
                .user(user1)
                .study(study2)
                .status(ApplicationStatus.APPROVED)
                .build();
        studyApplicationRepository.save(application2);

        application3 = StudyApplication.builder()
                .user(user2)
                .study(study1)
                .status(ApplicationStatus.REJECTED)
                .build();
        studyApplicationRepository.save(application3);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 신청한 내역 조회")
    void findByUserAndStudy() {
        // when
        Optional<StudyApplication> found = studyApplicationRepository.findByUserAndStudy(user1, study1);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
        assertThat(found.get().getStudy().getId()).isEqualTo(study1.getId());
        assertThat(found.get().getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 사용자가 신청한 모든 스터디 신청 내역 조회")
    void findByUserOrderByAppliedAtDesc() {
        // when
        List<StudyApplication> applications = studyApplicationRepository.findByUserOrderByAppliedAtDesc(user1);

        // then
        assertThat(applications).hasSize(2);
        assertThat(applications).allMatch(app -> app.getUser().getId().equals(user1.getId()));
        assertThat(applications.get(0).getAppliedAt()).isAfterOrEqualTo(applications.get(1).getAppliedAt());
    }

    @Test
    @DisplayName("특정 스터디에 신청한 모든 신청 내역 조회")
    void findByStudyOrderByAppliedAtDesc() {
        // when
        List<StudyApplication> applications = studyApplicationRepository.findByStudyOrderByAppliedAtDesc(study1);

        // then
        assertThat(applications).hasSize(2);
        assertThat(applications).allMatch(app -> app.getStudy().getId().equals(study1.getId()));
        assertThat(applications.get(0).getAppliedAt()).isAfterOrEqualTo(applications.get(1).getAppliedAt());
    }

    @Test
    @DisplayName("특정 스터디의 특정 상태 신청 내역 조회")
    void findByStudyAndStatusOrderByAppliedAtDesc() {
        // when
        List<StudyApplication> pendingApplications = studyApplicationRepository.findByStudyAndStatusOrderByAppliedAtDesc(study1, ApplicationStatus.PENDING);
        List<StudyApplication> approvedApplications = studyApplicationRepository.findByStudyAndStatusOrderByAppliedAtDesc(study1, ApplicationStatus.APPROVED);

        // then
        assertThat(pendingApplications).hasSize(1);
        assertThat(pendingApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(approvedApplications).hasSize(0);
    }

    @Test
    @DisplayName("특정 사용자의 특정 상태 신청 내역 조회")
    void findByUserAndStatusOrderByAppliedAtDesc() {
        // when
        List<StudyApplication> pendingApplications = studyApplicationRepository.findByUserAndStatusOrderByAppliedAtDesc(user1, ApplicationStatus.PENDING);
        List<StudyApplication> approvedApplications = studyApplicationRepository.findByUserAndStatusOrderByAppliedAtDesc(user1, ApplicationStatus.APPROVED);

        // then
        assertThat(pendingApplications).hasSize(1);
        assertThat(pendingApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(approvedApplications).hasSize(1);
        assertThat(approvedApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("특정 스터디의 승인된 신청 개수 조회")
    void countByStudyAndStatus() {
        // when
        long pendingCount = studyApplicationRepository.countByStudyAndStatus(study1, ApplicationStatus.PENDING);
        long approvedCount = studyApplicationRepository.countByStudyAndStatus(study1, ApplicationStatus.APPROVED);
        long rejectedCount = studyApplicationRepository.countByStudyAndStatus(study1, ApplicationStatus.REJECTED);

        // then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(approvedCount).isEqualTo(0);
        assertThat(rejectedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 이미 신청했는지 확인")
    void existsByUserAndStudy() {
        // when
        boolean exists1 = studyApplicationRepository.existsByUserAndStudy(user1, study1);
        boolean exists2 = studyApplicationRepository.existsByUserAndStudy(user1, study2);
        boolean exists3 = studyApplicationRepository.existsByUserAndStudy(user2, study2);

        // then
        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
        assertThat(exists3).isFalse();
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 승인된 신청이 있는지 확인")
    void existsByUserAndStudyAndStatus() {
        // when
        boolean approvedExists = studyApplicationRepository.existsByUserAndStudyAndStatus(user1, study2, ApplicationStatus.APPROVED);
        boolean pendingExists = studyApplicationRepository.existsByUserAndStudyAndStatus(user1, study1, ApplicationStatus.APPROVED);

        // then
        assertThat(approvedExists).isTrue();
        assertThat(pendingExists).isFalse();
    }

    @Test
    @DisplayName("특정 스터디의 모든 신청 개수 조회")
    void countByStudy() {
        // when
        long count1 = studyApplicationRepository.countByStudy(study1);
        long count2 = studyApplicationRepository.countByStudy(study2);

        // then
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 사용자가 신청한 스터디 개수 조회")
    void countByUser() {
        // when
        long count1 = studyApplicationRepository.countByUser(user1);
        long count2 = studyApplicationRepository.countByUser(user2);

        // then
        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 사용자가 승인된 스터디 신청 개수 조회")
    void countByUserAndStatus() {
        // when
        long approvedCount = studyApplicationRepository.countByUserAndStatus(user1, ApplicationStatus.APPROVED);
        long pendingCount = studyApplicationRepository.countByUserAndStatus(user1, ApplicationStatus.PENDING);
        long rejectedCount = studyApplicationRepository.countByUserAndStatus(user1, ApplicationStatus.REJECTED);

        // then
        assertThat(approvedCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(rejectedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 스터디의 대기중인 신청 개수 조회")
    void countPendingApplicationsByStudy() {
        // when
        long pendingCount = studyApplicationRepository.countPendingApplicationsByStudy(study1);

        // then
        assertThat(pendingCount).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 스터디의 승인된 신청 개수 조회")
    void countApprovedApplicationsByStudy() {
        // when
        long approvedCount = studyApplicationRepository.countApprovedApplicationsByStudy(study1);

        // then
        assertThat(approvedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자별 신청 내역 조회 (스터디 및 리더 정보 포함)")
    void findByUserWithStudyAndLeader() {
        // when
        List<StudyApplication> applications = studyApplicationRepository.findByUserWithStudyAndLeader(user1);

        // then
        assertThat(applications).hasSize(2);
        assertThat(applications).allMatch(app -> app.getUser().getId().equals(user1.getId()));
        assertThat(applications.get(0).getStudy()).isNotNull();
        assertThat(applications.get(0).getStudy().getLeader()).isNotNull();
    }

    @Test
    @DisplayName("스터디 신청 저장 및 조회")
    void saveAndFind() {
        // given
        StudyApplication newApplication = StudyApplication.builder()
                .user(user2)
                .study(study2)
                .status(ApplicationStatus.PENDING)
                .build();

        // when
        StudyApplication saved = studyApplicationRepository.save(newApplication);
        StudyApplication found = studyApplicationRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(user2.getId());
        assertThat(found.getStudy().getId()).isEqualTo(study2.getId());
        assertThat(found.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("신청 상태 변경")
    void updateApplicationStatus() {
        // given
        assertThat(application1.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        // when
        application1.approve();
        studyApplicationRepository.save(application1);
        StudyApplication updated = studyApplicationRepository.findById(application1.getId()).orElse(null);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updated.isApproved()).isTrue();
    }
} 