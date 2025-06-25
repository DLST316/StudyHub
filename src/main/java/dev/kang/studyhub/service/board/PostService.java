package dev.kang.studyhub.service.board;

import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostRepository;
import dev.kang.studyhub.domain.board.PostLike;
import dev.kang.studyhub.domain.board.PostLikeRepository;
import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * 게시글(Post) 서비스
 * 
 * 게시글 CRUD 및 추천/비추천 기능을 담당합니다.
 * 추천/비추천은 사용자별로 한 번만 가능하며, 취소도 가능합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

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

    /** 조회수 증가 */
    @Transactional
    public void increaseView(Long postId) {
        Post post = getPost(postId);
        post.setViewCount(post.getViewCount() + 1);
    }

    /**
     * 추천/비추천 처리
     * 
     * @param postId 게시글 ID
     * @param user 사용자
     * @param likeType 추천 또는 비추천 타입
     * @return 처리 결과 (추천/비추천 추가, 변경, 취소)
     */
    @Transactional
    public String toggleLike(Long postId, User user, PostLike.LikeType likeType) {
        Post post = getPost(postId);
        
        // 기존 추천/비추천 상태 확인
        PostLike existingLike = postLikeRepository.findByPostAndUser(post, user).orElse(null);
        
        if (existingLike == null) {
            // 새로운 추천/비추천 추가
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            newLike.setType(likeType);
            postLikeRepository.save(newLike);
            
            // 게시글의 추천/비추천 수 업데이트
            updatePostLikeCounts(post);
            
            return likeType == PostLike.LikeType.LIKE ? "추천되었습니다." : "비추천되었습니다.";
            
        } else if (existingLike.getType() == likeType) {
            // 같은 타입의 추천/비추천 취소
            postLikeRepository.delete(existingLike);
            
            // 게시글의 추천/비추천 수 업데이트
            updatePostLikeCounts(post);
            
            return likeType == PostLike.LikeType.LIKE ? "추천이 취소되었습니다." : "비추천이 취소되었습니다.";
            
        } else {
            // 다른 타입으로 변경 (추천 → 비추천 또는 비추천 → 추천)
            existingLike.setType(likeType);
            postLikeRepository.save(existingLike);
            
            // 게시글의 추천/비추천 수 업데이트
            updatePostLikeCounts(post);
            
            return likeType == PostLike.LikeType.LIKE ? "비추천에서 추천으로 변경되었습니다." : "추천에서 비추천으로 변경되었습니다.";
        }
    }

    /**
     * 게시글의 추천/비추천 수를 업데이트
     */
    private void updatePostLikeCounts(Post post) {
        long likeCount = postLikeRepository.countLikesByPost(post);
        long dislikeCount = postLikeRepository.countDislikesByPost(post);
        
        post.setLikeCount((int) likeCount);
        post.setDislikeCount((int) dislikeCount);
        postRepository.save(post);
    }

    /**
     * 사용자가 특정 게시글에 한 추천/비추천 상태 조회
     */
    public PostLike.LikeType getUserLikeStatus(Long postId, User user) {
        Post post = getPost(postId);
        return postLikeRepository.findByPostAndUser(post, user)
                .map(PostLike::getType)
                .orElse(null);
    }

    /**
     * 게시글의 현재 추천/비추천 수 조회
     */
    public LikeCounts getLikeCounts(Long postId) {
        Post post = getPost(postId);
        long likeCount = postLikeRepository.countLikesByPost(post);
        long dislikeCount = postLikeRepository.countDislikesByPost(post);
        
        return new LikeCounts((int) likeCount, (int) dislikeCount);
    }

    /**
     * 추천/비추천 수를 담는 DTO
     */
    public static class LikeCounts {
        private final int likeCount;
        private final int dislikeCount;

        public LikeCounts(int likeCount, int dislikeCount) {
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public int getDislikeCount() {
            return dislikeCount;
        }
    }

    /**
     * 게시판별 게시글 목록 조회 (페이징)
     */
    public Page<Post> findPostsByBoard(Board board, Pageable pageable) {
        return postRepository.findByBoardId(board.getId(), pageable);
    }

    /**
     * 게시글 ID로 조회
     */
    public Optional<Post> findById(Long postId) {
        return postRepository.findById(postId);
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public Post createPost(Board board, User user, String title, String content) {
        Post post = new Post();
        post.setBoard(board);
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setDislikeCount(0);
        return postRepository.save(post);
    }

    /**
     * 비추천 처리
     */
    @Transactional
    public String toggleDislike(Long postId, User user, PostLike.LikeType likeType) {
        return toggleLike(postId, user, PostLike.LikeType.DISLIKE);
    }
} 