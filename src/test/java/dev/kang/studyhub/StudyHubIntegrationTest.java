package dev.kang.studyhub;

import dev.kang.studyhub.board.entity.Board;
import dev.kang.studyhub.board.entity.Post;
import dev.kang.studyhub.board.entity.PostComment;
import dev.kang.studyhub.board.repository.BoardRepository;
import dev.kang.studyhub.board.repository.PostCommentRepository;
import dev.kang.studyhub.board.repository.PostLikeRepository;
import dev.kang.studyhub.board.repository.PostRepository;
import dev.kang.studyhub.user.entity.User;
import dev.kang.studyhub.user.exception.AlreadyExistsEmailException;
import dev.kang.studyhub.user.model.EducationStatus;
import dev.kang.studyhub.user.repository.UserRepository;
import dev.kang.studyhub.user.service.UserService;
import dev.kang.studyhub.user.dto.UserJoinForm;
import dev.kang.studyhub.study.entity.Study;
import dev.kang.studyhub.study.entity.StudyApplication;
import dev.kang.studyhub.study.entity.StudyComment;
import dev.kang.studyhub.study.model.ApplicationStatus;
import dev.kang.studyhub.study.repository.StudyApplicationRepository;
import dev.kang.studyhub.study.service.StudyService;
import dev.kang.studyhub.study.service.StudyApplicationService;
import dev.kang.studyhub.study.service.StudyCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.kang.studyhub.board.entity.PostLike.LikeType;
import dev.kang.studyhub.board.service.PostService;
import dev.kang.studyhub.board.service.PostCommentService;
import dev.kang.studyhub.common.service.ImageUploadService;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;

/**
 * StudyHub 애플리케이션의 통합 테스트
 * 
 * 테스트 대상:
 * - 전체 애플리케이션 컨텍스트 로딩
 * - 웹 계층과 서비스 계층의 연동
 * - 데이터베이스 연동
 * - Spring Security 설정
 * 
 * 테스트 시나리오:
 * - 애플리케이션 시작 및 컨텍스트 로딩
 * - 회원가입부터 로그인까지의 전체 플로우
 * - 웹 페이지 접근 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class StudyHubIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyApplicationService studyApplicationService;

    @Autowired
    private StudyCommentService studyCommentService;

    @Autowired
    private StudyApplicationRepository studyApplicationRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @SpyBean
    private ImageUploadService imageUploadService;

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로딩되어야 한다")
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로딩되는지 확인
        assertThat(userService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("홈페이지에 정상적으로 접근할 수 있어야 한다")
    void homePage_Accessible() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("StudyHub");
    }

    @Test
    @DisplayName("회원가입 페이지에 정상적으로 접근할 수 있어야 한다")
    void joinPage_Accessible() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/join", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("회원가입");
    }

    @Test
    @DisplayName("회원가입부터 데이터베이스 저장까지 전체 플로우가 정상적으로 동작해야 한다")
    void joinFlow_CompleteSuccess() {
        // given
        UserJoinForm joinForm = new UserJoinForm();
        joinForm.setName("통합 테스트 사용자");
        joinForm.setUsername("integration_user");
        joinForm.setEmail("integration@example.com");
        joinForm.setPassword("password123");
        joinForm.setUniversity("통합 테스트 대학교");
        joinForm.setMajor("통합 테스트 전공");
        joinForm.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm);

        // then
        Optional<User> savedUser = userRepository.findByEmail("integration@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("통합 테스트 사용자");
        assertThat(savedUser.get().getUniversity()).isEqualTo("통합 테스트 대학교");
        assertThat(savedUser.get().getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외가 발생해야 한다")
    void joinFlow_DuplicateEmail_ThrowsException() {
        // given
        UserJoinForm joinForm1 = new UserJoinForm();
        joinForm1.setName("첫 번째 사용자");
        joinForm1.setUsername("first_user");
        joinForm1.setEmail("duplicate@example.com");
        joinForm1.setPassword("password123");
        joinForm1.setEducationStatus(EducationStatus.ENROLLED);

        UserJoinForm joinForm2 = new UserJoinForm();
        joinForm2.setName("두 번째 사용자");
        joinForm2.setUsername("second_user");
        joinForm2.setEmail("duplicate@example.com");
        joinForm2.setPassword("password456");
        joinForm2.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm1);

        // then
        assertThat(userRepository.findByEmail("duplicate@example.com")).isPresent();
        
        // 중복 이메일로 가입 시도
        try {
            userService.join(joinForm2);
            assertThat(false).isTrue(); // 예외가 발생해야 함
        } catch (AlreadyExistsEmailException e) {
            assertThat(e.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
        }
    }

    @Test
    @DisplayName("비밀번호가 암호화되어 저장되어야 한다")
    void joinFlow_PasswordEncoded() {
        // given
        UserJoinForm joinForm = new UserJoinForm();
        joinForm.setName("암호화 테스트");
        joinForm.setUsername("encode_user");
        joinForm.setEmail("encode@example.com");
        joinForm.setPassword("plainPassword");
        joinForm.setEducationStatus(EducationStatus.ENROLLED);

        // when
        userService.join(joinForm);

        // then
        Optional<User> savedUser = userRepository.findByEmail("encode@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).isNotEqualTo("plainPassword");
        assertThat(savedUser.get().getPassword()).startsWith("$2a$"); // BCrypt 해시 형식
    }

    @Test
    @DisplayName("스터디 생성부터 신청, 승인, 댓글 작성까지 전체 플로우가 정상 동작해야 한다")
    void studyFullFlow_CompleteSuccess() {
        // 1. 스터디 개설자 회원가입
        UserJoinForm leaderForm = new UserJoinForm();
        leaderForm.setName("리더");
        leaderForm.setUsername("study_leader");
        leaderForm.setEmail("leader@example.com");
        leaderForm.setPassword("leaderpass");
        leaderForm.setEducationStatus(EducationStatus.ENROLLED);
        userService.join(leaderForm);
        User leader = userRepository.findByEmail("leader@example.com").orElseThrow();

        // 2. 신청자 회원가입
        UserJoinForm applicantForm = new UserJoinForm();
        applicantForm.setName("신청자");
        applicantForm.setUsername("study_applicant");
        applicantForm.setEmail("applicant@example.com");
        applicantForm.setPassword("applicantpass");
        applicantForm.setEducationStatus(EducationStatus.ENROLLED);
        userService.join(applicantForm);
        User applicant = userRepository.findByEmail("applicant@example.com").orElseThrow();

        // 3. 스터디 생성
        Study study = Study.builder()
                .title("통합테스트 스터디")
                .description("통합테스트용 스터디입니다.")
                .leader(leader)
                .recruitmentLimit(3)
                .requirement("테스트 전공")
                .build();
        studyService.createStudy(study);
        assertThat(study.getId()).isNotNull();

        // 4. 신청자가 스터디 신청
        studyApplicationService.apply(applicant, study);
        StudyApplication application = studyApplicationRepository.findByUserAndStudy(applicant, study).orElseThrow();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        // 5. 리더가 신청 승인
        studyApplicationService.approve(application.getId());
        StudyApplication approvedApp = studyApplicationRepository.findById(application.getId()).orElseThrow();
        assertThat(approvedApp.getStatus()).isEqualTo(ApplicationStatus.APPROVED);

        // 6. 신청자가 댓글 작성
        StudyComment comment = studyCommentService.createComment("통합테스트 댓글입니다.", applicant, study);
        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getContent()).isEqualTo("통합테스트 댓글입니다.");
    }

    @Test
    @DisplayName("게시판 전체 플로우: 게시글/댓글/추천/이미지 업로드/예외/권한 등")
    void communityFullFlow_CompleteSuccess() throws Exception {
        // 1. 게시판(Board) 생성
        Board board = new Board();
        board.setName("테스트게시판_" + java.util.UUID.randomUUID());
        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);

        // 2. 사용자 2명 회원가입
        UserJoinForm user1Form = new UserJoinForm();
        user1Form.setName("작성자");
        user1Form.setUsername("post_author");
        user1Form.setEmail("author@ex.com");
        user1Form.setPassword("pw1");
        user1Form.setEducationStatus(EducationStatus.ENROLLED);
        userService.join(user1Form);
        User author = userRepository.findByEmail("author@ex.com").orElseThrow();

        UserJoinForm user2Form = new UserJoinForm();
        user2Form.setName("추천자");
        user2Form.setUsername("post_liker");
        user2Form.setEmail("liker@ex.com");
        user2Form.setPassword("pw2");
        user2Form.setEducationStatus(EducationStatus.ENROLLED);
        userService.join(user2Form);
        User liker = userRepository.findByEmail("liker@ex.com").orElseThrow();

        // 3. 이미지 업로드(Mock)
        MockMultipartFile image = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1,2,3});
        org.mockito.Mockito.doReturn("https://mock.cloudinary.com/test.jpg")
            .when(imageUploadService).uploadImage(image);
        String imageUrl = imageUploadService.uploadImage(image);
        System.out.println("[TEST] imageUrl=" + imageUrl);
        assertThat(imageUploadService.isValidImageUrl(imageUrl)).isTrue();

        // 4. 게시글 작성 (이미지 포함)
        Post post = new Post();
        post.setBoard(board);
        post.setUser(author);
        post.setTitle("테스트 게시글");
        post.setContent("본문 내용 <img src='" + imageUrl + "'>");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setDislikeCount(0);
        post.setNotice(false);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        Post savedPost = postService.savePost(post);
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getContent()).contains(imageUrl);

        // 5. 게시글 목록/상세 조회
        List<Post> posts = postService.getPosts(board, org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        assertThat(posts).extracting("title").contains("테스트 게시글");
        Post foundPost = postService.getPost(savedPost.getId());
        assertThat(foundPost.getTitle()).isEqualTo("테스트 게시글");

        // 6. 게시글 조회수 증가
        postService.increaseView(savedPost.getId());
        assertThat(postService.getPost(savedPost.getId()).getViewCount()).isEqualTo(1);

        // 7. 댓글 작성/조회
        PostComment comment = new PostComment();
        comment.setPost(savedPost);
        comment.setUser(liker);
        comment.setContent("댓글 내용");
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        PostComment savedComment = postCommentService.saveComment(comment);
        assertThat(savedComment.getId()).isNotNull();
        List<PostComment> comments = postCommentService.getComments(savedPost);
        assertThat(comments).extracting("content").contains("댓글 내용");

        // 8. 댓글 삭제
        postCommentService.deleteComment(savedComment.getId());
        assertThat(postCommentService.getComments(savedPost)).isEmpty();

        // 9. 게시글 추천/비추천(토글, 취소, 변경)
        String likeMsg = postService.toggleLike(savedPost.getId(), liker, LikeType.LIKE);
        assertThat(likeMsg).contains("추천");
        assertThat(postService.getLikeCounts(savedPost.getId()).getLikeCount()).isEqualTo(1);
        // 같은 사용자가 다시 추천 → 취소
        String cancelMsg = postService.toggleLike(savedPost.getId(), liker, LikeType.LIKE);
        assertThat(cancelMsg).contains("취소");
        assertThat(postService.getLikeCounts(savedPost.getId()).getLikeCount()).isEqualTo(0);
        // 비추천
        String dislikeMsg = postService.toggleLike(savedPost.getId(), liker, LikeType.DISLIKE);
        assertThat(dislikeMsg).contains("비추천");
        assertThat(postService.getLikeCounts(savedPost.getId()).getDislikeCount()).isEqualTo(1);
        // 비추천 → 추천 변경
        String changeMsg = postService.toggleLike(savedPost.getId(), liker, LikeType.LIKE);
        assertThat(changeMsg).contains("추천");
        assertThat(postService.getLikeCounts(savedPost.getId()).getLikeCount()).isEqualTo(1);
        assertThat(postService.getLikeCounts(savedPost.getId()).getDislikeCount()).isEqualTo(0);

        // 10. 게시글 삭제
        postService.deletePost(savedPost.getId());
        assertThatThrownBy(() -> postService.getPost(savedPost.getId())).isInstanceOf(Exception.class);

        // 11. 예외/경계 상황: 없는 게시글/댓글 접근, 권한 없는 요청 등
        assertThatThrownBy(() -> postService.getPost(99999L)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> postCommentService.getComment(99999L)).isInstanceOf(Exception.class);
        // 권한 없는 사용자가 게시글/댓글 삭제 등은 실제 컨트롤러/시큐리티 통합에서 별도 검증 필요
    }
} 