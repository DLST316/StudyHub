package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글(Post) 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;

    /** 게시글 목록 조회 (페이징) */
    public Page<Post> getPosts(Board board, Pageable pageable) {
        return postRepository.findByBoardId(board.getId(), pageable);
    }

    /** 게시글 단건 조회 */
    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow();
    }

    /** 게시글 저장 */
    @Transactional
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    /** 추천수 증가 */
    @Transactional
    public void increaseLike(Long postId) {
        Post post = getPost(postId);
        post.setLikeCount(post.getLikeCount() + 1);
    }

    /** 비추천수 증가 */
    @Transactional
    public void increaseDislike(Long postId) {
        Post post = getPost(postId);
        post.setDislikeCount(post.getDislikeCount() + 1);
    }

    /** 조회수 증가 */
    @Transactional
    public void increaseView(Long postId) {
        Post post = getPost(postId);
        post.setViewCount(post.getViewCount() + 1);
    }
} 