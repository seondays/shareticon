package seondays.shareticon.api.config;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.config.AuditingConfig;

@ActiveProfiles("test")
@DataJpaTest
@Import(AuditingConfig.class)
public abstract class RepositoryTestSupport {

}
