package dev.kang.studyhub.domain.board;

import dev.kang.studyhub.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * PostLike 엔티티의 데이터 접근을 위한 리포지토리
 * 
 * 사용자별 추천/비추천 상태를 관리하는 데이터베이스 작업을 담당합니다.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 사용자가 특정 게시글에 한 추천/비추천 조회
     */
    Optional<PostLike> findByPostAndUser(Post post, User user);

    /**
     * 특정 사용자가 특정 게시글에 추천/비추천을 했는지 확인
     */
    boolean existsByPostAndUser(Post post, User user);

    /**
     * 특정 게시글의 추천 개수 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post AND pl.type = 'LIKE'")
    long countLikesByPost(@Param("post") Post post);

    /**
     * 특정 게시글의 비추천 개수 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post AND pl.type = 'DISLIKE'")
    long countDislikesByPost(@Param("post") Post post);

    /**
     * 특정 사용자가 특정 게시글에 한 추천/비추천 삭제
     */
    void deleteByPostAndUser(Post post, User user);
} 