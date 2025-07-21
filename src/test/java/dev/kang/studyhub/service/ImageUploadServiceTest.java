package dev.kang.studyhub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import dev.kang.studyhub.common.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * 이미지 업로드 서비스(ImageUploadService) 단위테스트
 * - Cloudinary 업로드, URL 유효성 등 검증
 */
class ImageUploadServiceTest {
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private MultipartFile multipartFile;
    @Mock
    private Uploader uploader;
    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Cloudinary uploader mock 연결
        given(cloudinary.uploader()).willReturn(uploader);
        // Cloudinary 설정값을 정상적으로 세팅
        ReflectionTestUtils.setField(imageUploadService, "cloudName", "real-cloud");
        ReflectionTestUtils.setField(imageUploadService, "apiKey", "real-key");
        ReflectionTestUtils.setField(imageUploadService, "apiSecret", "real-secret");
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_success() throws Exception {
        byte[] fileBytes = new byte[]{1,2,3};
        given(multipartFile.getBytes()).willReturn(fileBytes);
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg");
        given(uploader.upload(any(), any())).willReturn(uploadResult);
        String url = imageUploadService.uploadImage(multipartFile);
        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/sample.jpg");
    }

    @Test
    @DisplayName("Cloudinary 설정 미비시 예외 발생")
    void uploadImage_cloudinaryConfigError() {
        // 잘못된 설정값 세팅
        ReflectionTestUtils.setField(imageUploadService, "cloudName", "test-cloud");
        ReflectionTestUtils.setField(imageUploadService, "apiKey", "test-key");
        ReflectionTestUtils.setField(imageUploadService, "apiSecret", "test-secret");
        assertThatThrownBy(() -> imageUploadService.uploadImage(multipartFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary 설정이 필요합니다");
    }

    @Test
    @DisplayName("Cloudinary 업로드 실패시 예외 발생")
    void uploadImage_cloudinaryUploadFail() throws Exception {
        byte[] fileBytes = new byte[]{1,2,3};
        given(multipartFile.getBytes()).willReturn(fileBytes);
        given(uploader.upload(any(), any())).willThrow(new RuntimeException("업로드 실패"));
        assertThatThrownBy(() -> imageUploadService.uploadImage(multipartFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("이미지 업로드에 실패했습니다");
    }

    @Nested
    @DisplayName("이미지 URL 유효성 검사")
    class IsValidImageUrl {
        @Test
        @DisplayName("정상 이미지 URL")
        void validUrl() {
            assertThat(imageUploadService.isValidImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg")).isTrue();
            assertThat(imageUploadService.isValidImageUrl("http://example.com/img.png")).isTrue();
            assertThat(imageUploadService.isValidImageUrl("https://a.com/b.webp")).isTrue();
        }
        @Test
        @DisplayName("비정상 이미지 URL")
        void invalidUrl() {
            assertThat(imageUploadService.isValidImageUrl(null)).isFalse();
            assertThat(imageUploadService.isValidImageUrl("")).isFalse();
            assertThat(imageUploadService.isValidImageUrl("ftp://a.com/img.jpg")).isFalse();
            assertThat(imageUploadService.isValidImageUrl("https://a.com/file.txt")).isFalse();
        }
    }
} 