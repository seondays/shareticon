package seondays.shareticon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@EnableAsync
@EnableCaching
@SpringBootApplication
public class ShareticonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareticonApplication.class, args);
	}

}
