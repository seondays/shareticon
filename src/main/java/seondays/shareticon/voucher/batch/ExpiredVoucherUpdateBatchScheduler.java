package seondays.shareticon.voucher.batch;

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
public class ExpiredVoucherUpdateBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Clock clock;
    private final Job expiredVoucherUpdateJob;
    public static final String JOB_ID_PREFIX = "runExpiredVoucherUpdateJob";

    @Scheduled(cron = "0 0 0 * * ?")
    public void runExpiredVoucherUpdateJob() throws Exception {
        LocalDateTime now = LocalDateTime.now(clock);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", JOB_ID_PREFIX + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .addString("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .toJobParameters();

        jobLauncher.run(expiredVoucherUpdateJob, jobParameters);

    }
}
