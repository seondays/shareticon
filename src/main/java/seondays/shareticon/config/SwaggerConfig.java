package seondays.shareticon.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("shareticon API")
                        .description("shareticon 서비스의 백엔드 API 문서입니다")
                        .version("v1.0.0"));
    }
}
