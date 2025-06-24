package dev.kang.studyhub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 이미지 업로드 서비스
 * 
 * Cloudinary를 사용하여 이미지를 업로드하고 URL을 반환하는 서비스입니다.
 */
@Slf4j
@Service
public class ImageUploadService {

    private final Cloudinary cloudinary;
    
    @Value("${cloudinary.cloud-name:test-cloud}")
    private String cloudName;

    @Value("${cloudinary.api-key:test-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret:test-secret}")
    private String apiSecret;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * 이미지를 Cloudinary에 업로드합니다.
     * 
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL
     * @throws IOException 파일 업로드 중 오류 발생 시
     */
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // Cloudinary 설정 확인 (환경변수가 설정되지 않은 경우)
            if ("test-cloud".equals(cloudName) || "test-key".equals(apiKey) || "test-secret".equals(apiSecret)) {
                throw new IOException("Cloudinary 설정이 필요합니다. application.yml에서 cloudinary 설정을 확인해주세요.");
            }
            
            // Cloudinary에 이미지 업로드
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "studyhub",
                    "resource_type", "image"
                )
            );

            // 업로드된 이미지의 URL 반환
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("이미지 업로드 성공: {}", imageUrl);
            
            return imageUrl;
        } catch (Exception e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            if (e.getMessage().contains("Cloudinary 설정이 필요합니다")) {
                throw new IOException(e.getMessage());
            } else {
                throw new IOException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 이미지 URL이 유효한지 확인합니다.
     * 
     * @param imageUrl 확인할 이미지 URL
     * @return 유효한 URL인지 여부
     */
    public boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null && 
               (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) &&
               (imageUrl.contains(".jpg") || imageUrl.contains(".jpeg") || 
                imageUrl.contains(".png") || imageUrl.contains(".gif") ||
                imageUrl.contains(".webp"));
    }
} 