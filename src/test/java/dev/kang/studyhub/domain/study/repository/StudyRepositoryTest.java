package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.model.EducationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyRepository 클래스의 데이터 접근 계층 테스트
 * 
 * 테스트 대상:
 * - 스터디 저장 및 조회
 * - 스터디 검색 및 필터링
 * - 페이징 기능
 * - 사용자별 스터디 조회
 */
@DataJpaTest
class StudyRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private dev.kang.studyhub.domain.user.repository.UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Study study1;
    private Study study2;
    private Study study3;

    /**
     * 각 테스트 메서드 실행 전에 공통으로 사용할 테스트 데이터를 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser1 = User.builder()
                .name("테스트 사용자1")
                .email("test1@example.com")
                .password("password123")
                .university("테스트 대학교1")
                .major("컴퓨터공학과")
                .educationStatus(EducationStatus.ENROLLED)
                .role("USER")
                .build();

        testUser2 = User.builder()
                .name("테스트 사용자2")
                .email("test2@example.com")
                .password("password456")
                .university("테스트 대학교2")
                .major("전자공학과")
                .educationStatus(EducationStatus.GRADUATED)
                .role("USER")
                .build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // 테스트용 스터디 생성
        study1 = Study.builder()
                .title("스프링 부트 스터디")
                .description("스프링 부트 기초부터 심화까지")
                .leader(testUser1)
                .recruitmentLimit(5)
                .requirement("컴퓨터공학과 재학생")
                .deadline(LocalDate.now().plusDays(30))
                .build();

        study2 = Study.builder()
                .title("알고리즘 스터디")
                .description("코딩 테스트 준비")
                .leader(testUser1)
                .recruitmentLimit(3)
                .requirement("전공 무관")
                .deadline(LocalDate.now().plusDays(15))
                .build();

        study3 = Study.builder()
                .title("프로젝트 스터디")
                .description("웹 프로젝트 개발")
                .leader(testUser2)
                .recruitmentLimit(4)
                .requirement("웹 개발 경험자")
                .deadline(LocalDate.now().minusDays(5)) // 마감일 지남
                .build();

        studyRepository.save(study1);
        studyRepository.save(study2);
        studyRepository.save(study3);
    }

    @Test
    @DisplayName("스터디를 저장하고 ID로 조회할 수 있어야 한다")
    void saveAndFindById_Success() {
        // given
        Study newStudy = Study.builder()
                .title("새로운 스터디")
                .description("새로운 스터디 설명")
                .leader(testUser1)
                .recruitmentLimit(3)
                .build();

        // when
        Study savedStudy = studyRepository.save(newStudy);
        Study foundStudy = studyRepository.findById(savedStudy.getId()).orElse(null);

        // then
        assertThat(foundStudy).isNotNull();
        assertThat(foundStudy.getTitle()).isEqualTo("새로운 스터디");
        assertThat(foundStudy.getLeader().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("모든 스터디를 생성일 기준 내림차순으로 조회할 수 있어야 한다")
    void findAllOrderByCreatedAtDesc_Success() {
        // when
        List<Study> studies = studyRepository.findAllOrderByCreatedAtDesc();

        // then
        assertThat(studies).hasSize(3);
        // 생성일 기준 내림차순이므로 마지막에 생성된 study3이 첫 번째
        assertThat(studies.get(0).getTitle()).isEqualTo("프로젝트 스터디");
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 목록을 조회할 수 있어야 한다")
    void findByLeaderOrderByCreatedAtDesc_Success() {
        // when
        List<Study> user1Studies = studyRepository.findByLeaderOrderByCreatedAtDesc(testUser1);

        // then
        assertThat(user1Studies).hasSize(2);
        assertThat(user1Studies).allMatch(study -> study.getLeader().getId().equals(testUser1.getId()));
    }

    @Test
    @DisplayName("제목에 키워드가 포함된 스터디를 검색할 수 있어야 한다")
    void findByTitleContainingOrderByCreatedAtDesc_Success() {
        // when
        List<Study> springStudies = studyRepository.findByTitleContainingOrderByCreatedAtDesc("스프링");

        // then
        assertThat(springStudies).hasSize(1);
        assertThat(springStudies.get(0).getTitle()).isEqualTo("스프링 부트 스터디");
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 활성 스터디를 조회할 수 있어야 한다")
    void findActiveStudiesOrderByCreatedAtDesc_Success() {
        // when
        List<Study> activeStudies = studyRepository.findActiveStudiesOrderByCreatedAtDesc(LocalDate.now());

        // then
        assertThat(activeStudies).hasSize(2); // study1, study2만 활성
        assertThat(activeStudies).noneMatch(study -> study.getTitle().equals("프로젝트 스터디"));
    }

    @Test
    @DisplayName("페이징을 통해 스터디 목록을 조회할 수 있어야 한다")
    void findAllOrderByCreatedAtDesc_WithPaging_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 2); // 첫 페이지, 2개씩

        // when
        Page<Study> studyPage = studyRepository.findAllOrderByCreatedAtDesc(pageable);

        // then
        assertThat(studyPage.getContent()).hasSize(2);
        assertThat(studyPage.getTotalElements()).isEqualTo(3);
        assertThat(studyPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 개수를 조회할 수 있어야 한다")
    void countByLeader_Success() {
        // when
        long user1StudyCount = studyRepository.countByLeader(testUser1);
        long user2StudyCount = studyRepository.countByLeader(testUser2);

        // then
        assertThat(user1StudyCount).isEqualTo(2);
        assertThat(user2StudyCount).isEqualTo(1);
    }

    @Test
    @DisplayName("활성 스터디 개수를 조회할 수 있어야 한다")
    void countActiveStudies_Success() {
        // when
        long activeStudyCount = studyRepository.countActiveStudies(LocalDate.now());

        // then
        assertThat(activeStudyCount).isEqualTo(2); // study1, study2만 활성
    }

    @Test
    @DisplayName("스터디 정보를 수정할 수 있어야 한다")
    void updateStudy_Success() {
        // given
        String newTitle = "수정된 스터디 제목";
        String newDescription = "수정된 스터디 설명";

        // when
        study1.updateStudy(newTitle, newDescription, 10, "수정된 조건", LocalDate.now().plusDays(60));
        Study updatedStudy = studyRepository.save(study1);

        // then
        assertThat(updatedStudy.getTitle()).isEqualTo(newTitle);
        assertThat(updatedStudy.getDescription()).isEqualTo(newDescription);
        assertThat(updatedStudy.getRecruitmentLimit()).isEqualTo(10);
    }
} 