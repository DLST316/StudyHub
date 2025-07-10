package dev.kang.studyhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정 클래스
 * - OpenAPI 스펙 및 Swagger UI를 위한 기본 정보 제공
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 스펙을 생성하는 빈을 등록합니다.
     * Swagger UI 및 /v3/api-docs 경로에서 사용됩니다.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StudyHub API")
                        .description("스터디/취준생 커뮤니티 플랫폼 API 문서")
                        .version("1.0.0")
                );
    }
} 