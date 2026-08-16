package seondays.shareticon.api.image;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.api.config.TestValidatorConfig;
import seondays.shareticon.config.AuditingConfig;
import seondays.shareticon.config.QueryDslConfig;
import seondays.shareticon.image.S3ImageCleanupService;
import seondays.shareticon.image.S3ImageCleanupTaskRepository;

@ActiveProfiles("test")
@DataJpaTest
@Import({AuditingConfig.class, QueryDslConfig.class, TestValidatorConfig.class,
        S3ImageCleanupService.class})
class S3ImageCleanupServiceTest {

    @Autowired
    private S3ImageCleanupService cleanupService;

    @Autowired
    private S3ImageCleanupTaskRepository cleanupTaskRepository;

    @Test
    @DisplayName("reserve는 정리 대상 objectKey를 기록한다")
    void reserve() {
        //when
        cleanupService.reserve("voucher/key");

        //then
        assertThat(cleanupTaskRepository.findByObjectKey("voucher/key")).isPresent();
    }

    @Test
    @DisplayName("complete는 objectKey의 정리 기록을 제거한다")
    void complete() {
        //given
        cleanupService.reserve("voucher/key");

        //when
        cleanupService.complete("voucher/key");

        //then
        assertThat(cleanupTaskRepository.findByObjectKey("voucher/key")).isEmpty();
    }
}
