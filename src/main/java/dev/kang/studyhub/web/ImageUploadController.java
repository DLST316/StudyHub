package dev.kang.studyhub.web;

import dev.kang.studyhub.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 이미지 업로드 API 컨트롤러
 * 
 * 리치 텍스트 에디터에서 이미지를 업로드할 때 사용하는 API입니다.
 * AJAX 요청으로 이미지를 업로드하고 URL을 반환합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    /**
     * 이미지 업로드 API
     * 
     * @param file 업로드할 이미지 파일
     * @return 업로드 결과 (성공 시 URL, 실패 시 에러 메시지)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 파일 타입 검사
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 크기 검사 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB 이하여야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 업로드
            String imageUrl = imageUploadService.uploadImage(file);
            
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("message", "이미지 업로드가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            String errorMessage = e.getMessage();
            
            // Cloudinary 설정 오류인 경우 특별한 메시지 제공
            if (errorMessage.contains("Cloudinary 설정이 필요합니다")) {
                response.put("success", false);
                response.put("message", "이미지 업로드 기능을 사용하려면 Cloudinary 설정이 필요합니다. 관리자에게 문의하세요.");
                response.put("setupRequired", true);
            } else {
                response.put("success", false);
                response.put("message", "이미지 업로드에 실패했습니다: " + errorMessage);
            }
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 