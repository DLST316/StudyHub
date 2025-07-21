package dev.kang.studyhub.board.repository;

import dev.kang.studyhub.board.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 게시글 댓글 JPA 리포지토리
 */
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostId(Long postId);
} 