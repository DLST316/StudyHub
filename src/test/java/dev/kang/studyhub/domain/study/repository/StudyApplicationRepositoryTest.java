package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyApplicationRepository 클래스의 데이터 접근 계층 테스트
 * 
 * 테스트 대상:
 * - 스터디 신청 저장 및 조회
 * - 신청 상태별 조회
 * - 사용자별/스터디별 신청 조회
 * - 중복 신청 방지
 */
@DataJpaTest
class StudyApplicationRepositoryTest {

    @Autowired
    private StudyApplicationRepository studyApplicationRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private dev.kang.studyhub.domain.user.repository.UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Study testStudy1;
    private Study testStudy2;
    private StudyApplication application1;
    private StudyApplication application2;
    private StudyApplication application3;

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser1 = User.builder()
                .name("신청자1")
                .email("applicant1@example.com")
                .password("password123")
                .university("테스트 대학교1")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        testUser2 = User.builder()
                .name("신청자2")
                .email("applicant2@example.com")
                .password("password456")
                .university("테스트 대학교2")
                .major("전자공학과")
                .educationStatus(EducationStatus.GRADUATED)
                .role("USER")
                .build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // 테스트용 스터디 생성
        testStudy1 = Study.builder()
                .title("스프링 부트 스터디")
                .description("스프링 부트 기초부터 심화까지")
                .leader(testUser1)
                .recruitmentLimit(5)
                .requirement("컴퓨터공학과 재학생")
                .build();

        testStudy2 = Study.builder()
                .title("알고리즘 스터디")
                .description("코딩 테스트 준비")
                .leader(testUser2)
                .recruitmentLimit(3)
                .requirement("전공 무관")
                .build();

        studyRepository.save(testStudy1);
        studyRepository.save(testStudy2);

        // 테스트용 신청 생성
        application1 = StudyApplication.builder()
                .user(testUser1)
                .study(testStudy2)
                .status(ApplicationStatus.PENDING)
                .build();

        application2 = StudyApplication.builder()
                .user(testUser2)
                .study(testStudy1)
                .status(ApplicationStatus.APPROVED)
                .build();

        application3 = StudyApplication.builder()
                .user(testUser1)
                .study(testStudy1)
                .status(ApplicationStatus.REJECTED)
                .build();

        studyApplicationRepository.save(application1);
        studyApplicationRepository.save(application2);
        studyApplicationRepository.save(application3);
    }

    @Test
    @DisplayName("스터디 신청을 저장하고 ID로 조회할 수 있어야 한다")
    void saveAndFindById_Success() {
        // given - 새로운 사용자와 스터디를 생성하여 중복을 방지
        User newUser = User.builder()
                .name("새로운 신청자")
                .email("newapplicant@example.com")
                .password("password789")
                .university("새로운 대학교")
                .major("새로운 전공")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();
        userRepository.save(newUser);

        Study newStudy = Study.builder()
                .title("새로운 스터디")
                .description("새로운 스터디 설명")
                .leader(newUser)
                .recruitmentLimit(3)
                .build();
        studyRepository.save(newStudy);

        StudyApplication newApplication = StudyApplication.builder()
                .user(newUser)
                .study(newStudy)
                .status(ApplicationStatus.PENDING)
                .build();

        // when
        StudyApplication savedApplication = studyApplicationRepository.save(newApplication);
        StudyApplication foundApplication = studyApplicationRepository.findById(savedApplication.getId()).orElse(null);

        // then
        assertThat(foundApplication).isNotNull();
        assertThat(foundApplication.getUser().getId()).isEqualTo(newUser.getId());
        assertThat(foundApplication.getStudy().getId()).isEqualTo(newStudy.getId());
        assertThat(foundApplication.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 신청한 내역을 조회할 수 있어야 한다")
    void findByUserAndStudy_Success() {
        // when
        Optional<StudyApplication> foundApplication = studyApplicationRepository.findByUserAndStudy(testUser1, testStudy2);

        // then
        assertThat(foundApplication).isPresent();
        assertThat(foundApplication.get().getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 사용자가 신청한 모든 스터디 신청 내역을 조회할 수 있어야 한다")
    void findByUserOrderByAppliedAtDesc_Success() {
        // when
        List<StudyApplication> user1Applications = studyApplicationRepository.findByUserOrderByAppliedAtDesc(testUser1);

        // then
        assertThat(user1Applications).hasSize(2);
        assertThat(user1Applications).allMatch(app -> app.getUser().getId().equals(testUser1.getId()));
    }

    @Test
    @DisplayName("특정 스터디에 신청한 모든 신청 내역을 조회할 수 있어야 한다")
    void findByStudyOrderByAppliedAtDesc_Success() {
        // when
        List<StudyApplication> study1Applications = studyApplicationRepository.findByStudyOrderByAppliedAtDesc(testStudy1);

        // then
        assertThat(study1Applications).hasSize(2);
        assertThat(study1Applications).allMatch(app -> app.getStudy().getId().equals(testStudy1.getId()));
    }

    @Test
    @DisplayName("특정 스터디의 특정 상태 신청 내역을 조회할 수 있어야 한다")
    void findByStudyAndStatusOrderByAppliedAtDesc_Success() {
        // when
        List<StudyApplication> pendingApplications = studyApplicationRepository.findByStudyAndStatusOrderByAppliedAtDesc(testStudy1, ApplicationStatus.PENDING);
        List<StudyApplication> approvedApplications = studyApplicationRepository.findByStudyAndStatusOrderByAppliedAtDesc(testStudy1, ApplicationStatus.APPROVED);

        // then
        assertThat(pendingApplications).hasSize(0); // testStudy1에는 PENDING 신청이 없음
        assertThat(approvedApplications).hasSize(1);
        assertThat(approvedApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("특정 사용자의 특정 상태 신청 내역을 조회할 수 있어야 한다")
    void findByUserAndStatusOrderByAppliedAtDesc_Success() {
        // when
        List<StudyApplication> pendingApplications = studyApplicationRepository.findByUserAndStatusOrderByAppliedAtDesc(testUser1, ApplicationStatus.PENDING);
        List<StudyApplication> rejectedApplications = studyApplicationRepository.findByUserAndStatusOrderByAppliedAtDesc(testUser1, ApplicationStatus.REJECTED);

        // then
        assertThat(pendingApplications).hasSize(1);
        assertThat(rejectedApplications).hasSize(1);
        assertThat(pendingApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(rejectedApplications.get(0).getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("특정 스터디의 승인된 신청 개수를 조회할 수 있어야 한다")
    void countByStudyAndStatus_Success() {
        // when
        long approvedCount = studyApplicationRepository.countByStudyAndStatus(testStudy1, ApplicationStatus.APPROVED);
        long pendingCount = studyApplicationRepository.countByStudyAndStatus(testStudy1, ApplicationStatus.PENDING);

        // then
        assertThat(approvedCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 이미 신청했는지 확인할 수 있어야 한다")
    void existsByUserAndStudy_Success() {
        // when
        boolean exists1 = studyApplicationRepository.existsByUserAndStudy(testUser1, testStudy2);
        boolean exists2 = studyApplicationRepository.existsByUserAndStudy(testUser1, testStudy1);
        boolean exists3 = studyApplicationRepository.existsByUserAndStudy(testUser2, testStudy2);

        // then
        assertThat(exists1).isTrue();  // 신청한 경우
        assertThat(exists2).isTrue();  // 신청한 경우
        assertThat(exists3).isFalse(); // 신청하지 않은 경우
    }

    @Test
    @DisplayName("특정 사용자가 특정 스터디에 승인된 신청이 있는지 확인할 수 있어야 한다")
    void existsByUserAndStudyAndStatus_Success() {
        // when
        boolean hasApproved = studyApplicationRepository.existsByUserAndStudyAndStatus(testUser2, testStudy1, ApplicationStatus.APPROVED);
        boolean hasPending = studyApplicationRepository.existsByUserAndStudyAndStatus(testUser1, testStudy2, ApplicationStatus.PENDING);

        // then
        assertThat(hasApproved).isTrue();
        assertThat(hasPending).isTrue();
    }

    @Test
    @DisplayName("신청 상태를 승인으로 변경할 수 있어야 한다")
    void approveApplication_Success() {
        // when
        application1.approve();
        StudyApplication updatedApplication = studyApplicationRepository.save(application1);

        // then
        assertThat(updatedApplication.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updatedApplication.isApproved()).isTrue();
    }

    @Test
    @DisplayName("신청 상태를 거절로 변경할 수 있어야 한다")
    void rejectApplication_Success() {
        // when
        application1.reject();
        StudyApplication updatedApplication = studyApplicationRepository.save(application1);

        // then
        assertThat(updatedApplication.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(updatedApplication.isRejected()).isTrue();
    }

    @Test
    @DisplayName("특정 스터디의 대기중인 신청 개수를 조회할 수 있어야 한다")
    void countPendingApplicationsByStudy_Success() {
        // when
        long pendingCount = studyApplicationRepository.countPendingApplicationsByStudy(testStudy2);

        // then
        assertThat(pendingCount).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 스터디의 승인된 신청 개수를 조회할 수 있어야 한다")
    void countApprovedApplicationsByStudy_Success() {
        // when
        long approvedCount = studyApplicationRepository.countApprovedApplicationsByStudy(testStudy1);

        // then
        assertThat(approvedCount).isEqualTo(1);
    }
} 