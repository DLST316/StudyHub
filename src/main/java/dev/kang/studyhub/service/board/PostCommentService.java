package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 게시글 댓글 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;

    /** 댓글 목록 조회 */
    public List<PostComment> getComments(Post post) {
        return postCommentRepository.findByPostId(post.getId());
    }

    /** 댓글 단건 조회 */
    public PostComment getComment(Long commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    /** 댓글 저장 */
    @Transactional
    public PostComment saveComment(PostComment comment) {
        return postCommentRepository.save(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Long commentId) {
        postCommentRepository.deleteById(commentId);
    }
} 