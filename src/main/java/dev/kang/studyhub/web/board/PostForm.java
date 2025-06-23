package dev.kang.studyhub.web.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 게시글 작성/수정 폼 DTO
 */
@Data
public class PostForm {
    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    private String content;
} 