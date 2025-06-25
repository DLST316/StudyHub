package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostCommentRepository;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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

    /**
     * 게시글별 댓글 목록 조회
     */
    public List<PostComment> findCommentsByPost(Post post) {
        return postCommentRepository.findByPostId(post.getId());
    }

    /**
     * 댓글 ID로 조회
     */
    public Optional<PostComment> findById(Long commentId) {
        return postCommentRepository.findById(commentId);
    }

    /**
     * 댓글 생성
     */
    @Transactional
    public PostComment createComment(Post post, User user, String content) {
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        return postCommentRepository.save(comment);
    }

    /**
     * 댓글 삭제 (권한 검증 포함)
     */
    @Transactional
    public String deleteComment(PostComment comment, User user) {
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        postCommentRepository.delete(comment);
        return "댓글이 삭제되었습니다.";
    }
} 