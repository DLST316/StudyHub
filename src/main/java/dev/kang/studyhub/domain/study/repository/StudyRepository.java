package dev.kang.studyhub.domain.study.repository;

import dev.kang.studyhub.domain.study.entity.Study;
import dev.kang.studyhub.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Study 엔티티의 데이터 접근을 위한 리포지토리
 * 
 * 스터디 관련 데이터베이스 작업을 담당합니다.
 * - 스터디 CRUD 작업
 * - 스터디 검색 및 필터링
 * - 사용자별 스터디 조회
 */
public interface StudyRepository extends JpaRepository<Study, Long> {

    /**
     * 모든 스터디를 생성일 기준 내림차순으로 조회
     */
    @Query("SELECT s FROM Study s ORDER BY s.createdAt DESC")
    List<Study> findAllOrderByCreatedAtDesc();

    /**
     * 모든 스터디를 페이징하여 생성일 기준 내림차순으로 조회
     */
    @Query("SELECT s FROM Study s ORDER BY s.createdAt DESC")
    Page<Study> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 사용자가 개설한 스터디 목록 조회
     */
    List<Study> findByLeaderOrderByCreatedAtDesc(User leader);

    /**
     * 특정 사용자가 개설한 스터디 목록을 페이징하여 조회
     */
    Page<Study> findByLeaderOrderByCreatedAtDesc(User leader, Pageable pageable);

    /**
     * 제목에 특정 키워드가 포함된 스터디 검색
     */
    @Query("SELECT s FROM Study s WHERE s.title LIKE %:keyword% ORDER BY s.createdAt DESC")
    List<Study> findByTitleContainingOrderByCreatedAtDesc(@Param("keyword") String keyword);

    /**
     * 제목에 특정 키워드가 포함된 스터디를 페이징하여 검색
     */
    @Query("SELECT s FROM Study s WHERE s.title LIKE %:keyword% ORDER BY s.createdAt DESC")
    Page<Study> findByTitleContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 모집 마감일이 지나지 않은 스터디 목록 조회
     */
    @Query("SELECT s FROM Study s WHERE s.deadline IS NULL OR s.deadline >= :today ORDER BY s.createdAt DESC")
    List<Study> findActiveStudiesOrderByCreatedAtDesc(@Param("today") LocalDate today);

    /**
     * 모집 마감일이 지나지 않은 스터디를 페이징하여 조회
     */
    @Query("SELECT s FROM Study s WHERE s.deadline IS NULL OR s.deadline >= :today ORDER BY s.createdAt DESC")
    Page<Study> findActiveStudiesOrderByCreatedAtDesc(@Param("today") LocalDate today, Pageable pageable);

    /**
     * 특정 사용자가 개설한 스터디 개수 조회
     */
    long countByLeader(User leader);

    /**
     * 모집 마감일이 지나지 않은 스터디 개수 조회
     */
    @Query("SELECT COUNT(s) FROM Study s WHERE s.deadline IS NULL OR s.deadline >= :today")
    long countActiveStudies(@Param("today") LocalDate today);
} 