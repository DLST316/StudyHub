package dev.kang.studyhub.web.board;

import dev.kang.studyhub.board.dto.PostForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostForm 유효성 검사 테스트
 */
class PostFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 PostForm - 검증 통과")
    void validPostForm() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("테스트 제목");
        postForm.setContent("테스트 내용");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("제목이 비어있는 경우 - 검증 실패")
    void emptyTitle() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("");
        postForm.setContent("테스트 내용");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("제목을 입력하세요.");
    }

    @Test
    @DisplayName("제목이 null인 경우 - 검증 실패")
    void nullTitle() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle(null);
        postForm.setContent("테스트 내용");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("제목을 입력하세요.");
    }

    @Test
    @DisplayName("제목이 공백만 있는 경우 - 검증 실패")
    void whitespaceOnlyTitle() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("   ");
        postForm.setContent("테스트 내용");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("제목을 입력하세요.");
    }

    @Test
    @DisplayName("내용이 비어있는 경우 - 검증 실패")
    void emptyContent() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("테스트 제목");
        postForm.setContent("");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용을 입력하세요.");
    }

    @Test
    @DisplayName("내용이 null인 경우 - 검증 실패")
    void nullContent() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("테스트 제목");
        postForm.setContent(null);

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용을 입력하세요.");
    }

    @Test
    @DisplayName("내용이 공백만 있는 경우 - 검증 실패")
    void whitespaceOnlyContent() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("테스트 제목");
        postForm.setContent("   ");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용을 입력하세요.");
    }

    @Test
    @DisplayName("제목과 내용 모두 비어있는 경우 - 검증 실패")
    void bothTitleAndContentEmpty() {
        // given
        PostForm postForm = new PostForm();
        postForm.setTitle("");
        postForm.setContent("");

        // when
        Set<ConstraintViolation<PostForm>> violations = validator.validate(postForm);

        // then
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(violation -> 
            violation.getPropertyPath().toString().equals("title") && 
            violation.getMessage().equals("제목을 입력하세요.")
        );
        assertThat(violations).anyMatch(violation -> 
            violation.getPropertyPath().toString().equals("content") && 
            violation.getMessage().equals("내용을 입력하세요.")
        );
    }
} 