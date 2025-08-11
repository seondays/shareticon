package seondays.shareticon.image.batch;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class S3ImageCleanupBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job voucherImageCleanupJob;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 1 1/2 ?", zone = "Asia/Seoul")
    public void runVoucherImageCleanupJob() {
        try {
            LocalDateTime now = LocalDateTime.now(clock);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobId", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .addString("executionDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .toJobParameters();

            jobLauncher.run(voucherImageCleanupJob, jobParameters);
        } catch (Exception e) {
            log.error("이미지 삭제 작업 실패");
        }
    }
}
