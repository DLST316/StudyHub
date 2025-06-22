package dev.kang.studyhub.service.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.entity.StudyApplication;
import dev.kang.studyhub.domain.study.model.ApplicationStatus;
import dev.kang.studyhub.domain.study.repository.StudyApplicationRepository;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 스터디 신청 관련 비즈니스 로직을 담당하는 서비스 클래스
 *
 * 주요 기능:
 * - 스터디 신청/취소/상태 변경
 * - 신청 내역 조회
 * - 신청 중복/승인/거절 등 상태 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyApplicationService {

    private final StudyApplicationRepository studyApplicationRepository;

    /**
     * 스터디 신청
     */
    @Transactional
    public StudyApplication apply(User user, Study study) {
        if (studyApplicationRepository.existsByUserAndStudy(user, study)) {
            throw new IllegalStateException("이미 신청한 스터디입니다.");
        }
        StudyApplication application = StudyApplication.builder()
                .user(user)
                .study(study)
                .status(ApplicationStatus.PENDING)
                .build();
        return studyApplicationRepository.save(application);
    }

    /**
     * 신청 취소
     */
    @Transactional
    public void cancel(Long applicationId, User user) {
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));
        if (!application.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인만 신청을 취소할 수 있습니다.");
        }
        studyApplicationRepository.delete(application);
    }

    /**
     * 신청 승인
     */
    @Transactional
    public void approve(Long applicationId) {
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));
        
        Study study = application.getStudy();
        
        // 인원 제한 체크
        if (study.getRecruitmentLimit() != null) {
            long approvedCount = countApprovedApplications(study);
            if (approvedCount >= study.getRecruitmentLimit()) {
                throw new IllegalStateException("모집 인원이 가득 찼습니다. (현재: " + approvedCount + "/" + study.getRecruitmentLimit() + ")");
            }
        }
        
        application.approve();
        studyApplicationRepository.save(application);
    }

    /**
     * 신청 거절
     */
    @Transactional
    public void reject(Long applicationId) {
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));
        application.reject();
        studyApplicationRepository.save(application);
    }

    /**
     * 특정 사용자의 신청 내역 조회
     */
    public List<StudyApplication> findByUser(User user) {
        return studyApplicationRepository.findByUserOrderByAppliedAtDesc(user);
    }

    /**
     * 특정 스터디의 신청 내역 조회
     */
    public List<StudyApplication> findByStudy(Study study) {
        return studyApplicationRepository.findByStudyOrderByAppliedAtDesc(study);
    }

    /**
     * 특정 스터디의 승인된 신청 개수 조회
     */
    public long countApprovedApplications(Study study) {
        return studyApplicationRepository.countByStudyAndStatus(study, ApplicationStatus.APPROVED);
    }

    /**
     * 특정 스터디의 대기중인 신청 개수 조회
     */
    public long countPendingApplications(Study study) {
        return studyApplicationRepository.countByStudyAndStatus(study, ApplicationStatus.PENDING);
    }

    /**
     * 특정 사용자가 특정 스터디에 이미 신청했는지 확인
     */
    public boolean hasApplied(User user, Study study) {
        return studyApplicationRepository.existsByUserAndStudy(user, study);
    }

    /**
     * 특정 사용자가 특정 스터디에 승인된 신청이 있는지 확인
     */
    public boolean isApproved(User user, Study study) {
        return studyApplicationRepository.existsByUserAndStudyAndStatus(user, study, ApplicationStatus.APPROVED);
    }

    /**
     * 특정 사용자의 특정 스터디 신청 내역 조회
     */
    public Optional<StudyApplication> findByUserAndStudy(User user, Study study) {
        return studyApplicationRepository.findByUserAndStudy(user, study);
    }

    /**
     * 특정 사용자가 스터디에 승인된 멤버인지 확인
     * 스터디 개설자도 포함합니다.
     */
    public boolean isApprovedMember(User user, Study study) {
        // 스터디 개설자는 항상 승인된 멤버로 간주
        if (study.isLeader(user)) {
            return true;
        }
        
        // 승인된 신청이 있는지 확인
        return isApproved(user, study);
    }

    /**
     * 특정 사용자가 승인받은 스터디 목록 조회
     */
    public List<Study> findApprovedStudiesByUser(User user) {
        return studyApplicationRepository.findByUserAndStatusOrderByAppliedAtDesc(user, ApplicationStatus.APPROVED)
                .stream()
                .map(StudyApplication::getStudy)
                .toList();
    }
} 