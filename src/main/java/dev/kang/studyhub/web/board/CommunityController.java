package dev.kang.studyhub.web.board;

import dev.kang.studyhub.domain.board.Board;
import dev.kang.studyhub.domain.board.Post;
import dev.kang.studyhub.domain.board.PostComment;
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
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);
        postService.increaseView(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getComments(post));
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

    /** 추천 */
    @PostMapping("/post/{id}/like")
    public String like(@PathVariable Long id) {
        postService.increaseLike(id);
        return "redirect:/community/post/" + id;
    }

    /** 비추천 */
    @PostMapping("/post/{id}/dislike")
    public String dislike(@PathVariable Long id) {
        postService.increaseDislike(id);
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