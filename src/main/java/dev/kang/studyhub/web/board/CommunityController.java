package dev.kang.studyhub.web.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
import dev.kang.studyhub.domain.board.PostLike;
import dev.kang.studyhub.domain.user.entity.User;
import dev.kang.studyhub.service.board.BoardService;
import dev.kang.studyhub.service.board.PostService;
import dev.kang.studyhub.service.board.PostCommentService;
import dev.kang.studyhub.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 커뮤니티(게시판) 컨트롤러
 * 게시글 목록, 상세, 글쓰기, 추천/비추천, 댓글 등 처리
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final BoardService boardService;
    private final PostService postService;
    private final PostCommentService commentService;
    private final UserService userService;

    /** 게시글 목록 */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Board board = boardService.getCommunityBoard();
        Page<Post> posts = postService.getPosts(board, PageRequest.of(page, 20));
        model.addAttribute("posts", posts);
        return "community/community";
    }

    /** 게시글 상세 */
    @GetMapping("/post/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.getPost(id);
        postService.increaseView(id);
        
        // 현재 사용자의 추천/비추천 상태 확인
        PostLike.LikeType userLikeStatus = null;
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userLikeStatus = postService.getUserLikeStatus(id, user);
            }
        }
        
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getComments(post));
        model.addAttribute("userLikeStatus", userLikeStatus);
        return "community/post-detail";
    }

    /** 글쓰기 폼 */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "community/post-form";
    }

    /** 글쓰기 처리 */
    @PostMapping("/write")
    public String write(@Valid @ModelAttribute PostForm postForm, BindingResult result, @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) return "community/post-form";
        Board board = boardService.getCommunityBoard();
        // 인증 사용자 조회
        if (userDetails == null) return "redirect:/login";
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Post post = new Post();
        post.setBoard(board);
        post.setUser(user);
        post.setTitle(postForm.getTitle());
        post.setContent(postForm.getContent());
        postService.savePost(post);
        return "redirect:/community";
    }

    /** 추천/비추천 처리 */
    @PostMapping("/post/{id}/like")
    public String toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        
        String result = postService.toggleLike(id, user, PostLike.LikeType.LIKE);
        // TODO: 결과 메시지를 세션에 저장하여 표시할 수 있도록 개선
        
        return "redirect:/community/post/" + id;
    }

    /** 비추천 처리 */
    @PostMapping("/post/{id}/dislike")
    public String toggleDislike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        
        String result = postService.toggleLike(id, user, PostLike.LikeType.DISLIKE);
        // TODO: 결과 메시지를 세션에 저장하여 표시할 수 있도록 개선
        
        return "redirect:/community/post/" + id;
    }

    /** 댓글 등록 */
    @PostMapping("/post/{id}/comment")
    public String comment(@PathVariable Long id, @RequestParam String content, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.getPost(id);
        if (userDetails == null) return "redirect:/login";
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        commentService.saveComment(comment);
        return "redirect:/community/post/" + id;
    }
} 