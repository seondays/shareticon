package seondays.shareticon.api.config;

import java.time.Clock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import seondays.shareticon.group.RandomCodeFactory;

@EnableScheduling
@EnableRetry
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport extends RedisTestContainer {

    @MockitoSpyBean
     protected RandomCodeFactory randomCodeFactory;

    @MockitoBean
    protected Clock clock;
}
