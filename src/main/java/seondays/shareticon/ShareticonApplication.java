package seondays.shareticon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class ShareticonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareticonApplication.class, args);
	}

}
