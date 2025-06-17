package seondays.shareticon.api.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestValidatorConfig {

    // DTO 검증을 수행할 validator에서 우리가 지정한 clock 객체를 사용하도록 설정한다
    @Bean
    public Validator validator(Clock clock) {
        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .clockProvider(() -> clock)
                .buildValidatorFactory();
        return factory.getValidator();
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(Instant.parse("2024-12-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    }

}

