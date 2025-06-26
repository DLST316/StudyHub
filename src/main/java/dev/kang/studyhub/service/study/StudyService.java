package dev.kang.studyhub.service.study;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.study.repository.StudyRepository;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 스터디 관련 비즈니스 로직을 담당하는 서비스 클래스
 *
 * 주요 기능:
 * - 스터디 생성, 수정, 삭제
 * - 스터디 목록/상세 조회
 * - 검색, 페이징, 필터링
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyApplicationService studyApplicationService;
    private final StudyCommentService studyCommentService;

    /**
     * 스터디 생성
     */
    @Transactional
    public Study createStudy(Study study) {
        return studyRepository.save(study);
    }

    /**
     * 스터디 수정
     */
    @Transactional
    public Study updateStudy(Study study, String title, String description, Integer recruitmentLimit, String requirement, LocalDate deadline) {
        study.updateStudy(title, description, recruitmentLimit, requirement, deadline);
        return studyRepository.save(study);
    }

    /**
     * 스터디 삭제
     * 스터디와 관련된 댓글들과 신청 내역을 먼저 삭제한 후 스터디를 삭제합니다.
     */
    @Transactional
    public void deleteStudy(Study study) {
        // 스터디와 관련된 신청 내역들을 먼저 삭제
        studyApplicationService.deleteAllByStudy(study);
        
        // 스터디와 관련된 댓글들을 먼저 삭제
        List<dev.kang.studyhub.domain.study.entity.StudyComment> comments = studyCommentService.getCommentsByStudy(study);
        for (dev.kang.studyhub.domain.study.entity.StudyComment comment : comments) {
            studyCommentService.deleteComment(comment.getId(), comment.getUser());
        }
        
        // 스터디 삭제
        studyRepository.delete(study);
    }

    /**
     * 스터디 ID로 조회
     */
    public Optional<Study> findById(Long id) {
        return studyRepository.findById(id);
    }

    /**
     * 전체 스터디 목록 조회 (최신순)
     */
    public List<Study> findAll() {
        return studyRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 전체 스터디 목록 페이징 조회 (최신순)
     */
    public Page<Study> findAll(Pageable pageable) {
        return studyRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    /**
     * 특정 사용자가 개설한 스터디 목록 조회
     */
    public List<Study> findByLeader(User leader) {
        return studyRepository.findByLeaderOrderByCreatedAtDesc(leader);
    }

    /**
     * 제목 키워드로 검색
     */
    public List<Study> searchByTitle(String keyword) {
        return studyRepository.findByTitleContainingOrderByCreatedAtDesc(keyword);
    }

    /**
     * 모집 마감일이 지나지 않은 활성 스터디 목록 조회
     */
    public List<Study> findActiveStudies() {
        return studyRepository.findActiveStudiesOrderByCreatedAtDesc(LocalDate.now());
    }

    /**
     * 모집 마감일이 지나지 않은 스터디 개수 조회
     */
    public long countActiveStudies() {
        return studyRepository.countActiveStudies(LocalDate.now());
    }

    /**
     * 특정 사용자가 개설한 스터디 개수 조회
     */
    public long countByLeader(User leader) {
        return studyRepository.countByLeader(leader);
    }

    /**
     * 최근 스터디 목록 조회 (홈페이지용)
     * 최신순으로 정렬된 최근 스터디들을 반환합니다.
     */
    public List<Study> findRecentStudies() {
        return studyRepository.findAllOrderByCreatedAtDesc();
    }
} 