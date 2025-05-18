package seondays.shareticon.api.config;

import java.time.Clock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import seondays.shareticon.group.RandomCodeFactory;
import seondays.shareticon.image.ImageService;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    private static final String REDIS_IMAGE = "redis:7.4.1-alpine";
    private static final int REDIS_PORT = 6379;

    @ServiceConnection(name = "redis")
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                    .withExposedPorts(REDIS_PORT)
                    .withReuse(true);

    @MockitoSpyBean
     protected RandomCodeFactory randomCodeFactory;

    @MockitoBean
    protected ImageService imageService;

    @MockitoBean
    protected Clock clock;
}
