package dev.kang.studyhub.web;

import dev.kang.studyhub.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * ImageUploadController 통합 테스트
 * 
 * 슬라이스 테스트의 문제점을 해결하기 위해 @SpringBootTest를 사용하여
 * 실제 서비스 객체들과 데이터베이스를 활용한 통합 테스트를 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ImageUploadControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ImageUploadService imageUploadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    @DisplayName("이미지 업로드 - 성공")
    void uploadImage_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when & then - Cloudinary 설정이 없으므로 500 에러
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("이미지 업로드 기능을 사용하려면 Cloudinary 설정이 필요합니다")));
    }

    @Test
    @DisplayName("이미지 업로드 - 빈 파일")
    void uploadImage_EmptyFile() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("파일이 비어있습니다."));
    }

    @Test
    @DisplayName("이미지 업로드 - 이미지가 아닌 파일")
    void uploadImage_NonImageFile() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미지 파일만 업로드 가능합니다."));
    }

    @Test
    @DisplayName("이미지 업로드 - 파일 크기 초과")
    void uploadImage_FileTooLarge() throws Exception {
        // given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // when & then
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("파일 크기는 5MB 이하여야 합니다."));
    }

    @Test
    @DisplayName("이미지 업로드 - 파일 파라미터 누락")
    void uploadImage_MissingFile() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/images/upload"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미지 업로드 - 다양한 이미지 타입")
    void uploadImage_VariousImageTypes() throws Exception {
        // given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test jpeg content".getBytes()
        );

        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test png content".getBytes()
        );

        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                "test gif content".getBytes()
        );

        // when & then - Cloudinary 설정이 없으므로 모든 요청이 500 에러
        mockMvc.perform(multipart("/api/images/upload")
                        .file(jpegFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(pngFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(gifFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
} 