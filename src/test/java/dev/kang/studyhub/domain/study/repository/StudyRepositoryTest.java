package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyRepository 통합 테스트
 *
 * - 스터디 CRUD 작업
 * - 스터디 검색 및 필터링
 * - 사용자별 스터디 조회
 * - 모집 마감일 기준 조회
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StudyRepositoryTest {
    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    private User leader1;
    private User leader2;
    private Study study1;
    private Study study2;
    private Study study3;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        leader1 = User.builder()
                .name("스터디장1")
                .email("leader1@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(leader1);

        leader2 = User.builder()
                .name("스터디장2")
                .email("leader2@test.com")
                .password("password")
                .role("USER")
                .isBlocked(false)
                .build();
        userRepository.save(leader2);

        // 테스트용 스터디 생성
        study1 = Study.builder()
                .title("Java 스터디")
                .description("Java 기초부터 심화까지")
                .leader(leader1)
                .recruitmentLimit(5)
                .requirement("Java 기초 지식")
                .deadline(LocalDate.now().plusDays(7))
                .build();
        studyRepository.save(study1);

        study2 = Study.builder()
                .title("Spring Boot 스터디")
                .description("Spring Boot 실전 프로젝트")
                .leader(leader1)
                .recruitmentLimit(3)
                .requirement("Java, Spring 기초")
                .deadline(LocalDate.now().plusDays(14))
                .build();
        studyRepository.save(study2);

        study3 = Study.builder()
                .title("Python 데이터 분석")
                .description("Python을 이용한 데이터 분석")
                .leader(leader2)
                .recruitmentLimit(4)
                .requirement("Python 기초")
                .deadline(LocalDate.now().minusDays(1)) // 마감된 스터디
                .build();
        studyRepository.save(study3);
    }

    @Test
    @DisplayName("모든 스터디를 생성일 기준 내림차순으로 조회")
    void findAllOrderByCreatedAtDesc() {
        // when
        List<Study> studies = studyRepository.findAllOrderByCreatedAtDesc();

        // then
        assertThat(studies).hasSize(3);
        assertThat(studies.get(0).getCreatedAt()).isAfterOrEqualTo(studies.get(1).getCreatedAt());
        assertThat(studies.get(1).getCreatedAt()).isAfterOrEqualTo(studies.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("모든 스터디를 페이징하여 생성일 기준 내림차순으로 조회")
    void findAllOrderByCreatedAtDesc_withPaging() {
        // when
        Page<Study> page = studyRepository.findAllOrderByCreatedAtDesc(PageRequest.of(0, 2));

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent().get(0).getCreatedAt()).isAfterOrEqualTo(page.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 목록 조회")
    void findByLeaderOrderByCreatedAtDesc() {
        // when
        List<Study> studies = studyRepository.findByLeaderOrderByCreatedAtDesc(leader1);

        // then
        assertThat(studies).hasSize(2);
        assertThat(studies).allMatch(study -> study.getLeader().getId().equals(leader1.getId()));
        assertThat(studies.get(0).getCreatedAt()).isAfterOrEqualTo(studies.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 목록을 페이징하여 조회")
    void findByLeaderOrderByCreatedAtDesc_withPaging() {
        // when
        Page<Study> page = studyRepository.findByLeaderOrderByCreatedAtDesc(leader1, PageRequest.of(0, 1));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent().get(0).getLeader().getId()).isEqualTo(leader1.getId());
    }

    @Test
    @DisplayName("제목에 특정 키워드가 포함된 스터디 검색")
    void findByTitleContainingOrderByCreatedAtDesc() {
        // when
        List<Study> studies = studyRepository.findByTitleContainingOrderByCreatedAtDesc("Java");

        // then
        assertThat(studies).hasSize(1);
        assertThat(studies.get(0).getTitle()).contains("Java");
    }

    @Test
    @DisplayName("제목에 특정 키워드가 포함된 스터디를 페이징하여 검색")
    void findByTitleContainingOrderByCreatedAtDesc_withPaging() {
        // when
        Page<Study> page = studyRepository.findByTitleContainingOrderByCreatedAtDesc("스터디", PageRequest.of(0, 2));

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent()).allMatch(study -> study.getTitle().contains("스터디"));
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 스터디 목록 조회")
    void findActiveStudiesOrderByCreatedAtDesc() {
        // when
        List<Study> studies = studyRepository.findActiveStudiesOrderByCreatedAtDesc(LocalDate.now());

        // then
        assertThat(studies).hasSize(2);
        assertThat(studies).allMatch(study -> 
                study.getDeadline() == null || study.getDeadline().isAfter(LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 스터디를 페이징하여 조회")
    void findActiveStudiesOrderByCreatedAtDesc_withPaging() {
        // when
        Page<Study> page = studyRepository.findActiveStudiesOrderByCreatedAtDesc(LocalDate.now(), PageRequest.of(0, 1));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getDeadline()).isAfter(LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("특정 사용자가 개설한 스터디 개수 조회")
    void countByLeader() {
        // when
        long count = studyRepository.countByLeader(leader1);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("모집 마감일이 지나지 않은 스터디 개수 조회")
    void countActiveStudies() {
        // when
        long count = studyRepository.countActiveStudies(LocalDate.now());

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("스터디 저장 및 조회")
    void saveAndFind() {
        // given
        Study newStudy = Study.builder()
                .title("새로운 스터디")
                .description("새로운 스터디입니다")
                .leader(leader1)
                .recruitmentLimit(3)
                .requirement("기초 지식")
                .deadline(LocalDate.now().plusDays(10))
                .build();

        // when
        Study saved = studyRepository.save(newStudy);
        Study found = studyRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("새로운 스터디");
        assertThat(found.getLeader().getId()).isEqualTo(leader1.getId());
    }

    @Test
    @DisplayName("스터디 수정")
    void updateStudy() {
        // given
        String newTitle = "수정된 Java 스터디";
        String newDescription = "수정된 설명";

        // when
        study1.updateStudy(newTitle, newDescription, 6, "수정된 요구사항", LocalDate.now().plusDays(10));
        studyRepository.save(study1);
        Study updated = studyRepository.findById(study1.getId()).orElse(null);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo(newTitle);
        assertThat(updated.getDescription()).isEqualTo(newDescription);
        assertThat(updated.getRecruitmentLimit()).isEqualTo(6);
    }
} 