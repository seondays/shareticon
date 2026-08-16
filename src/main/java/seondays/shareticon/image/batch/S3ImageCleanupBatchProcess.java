package seondays.shareticon.image.batch;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.image.S3ImageCleanupTask;
import seondays.shareticon.image.S3ImageCleanupTaskRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3ImageCleanupBatchProcess {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ImageService imageService;
    private final S3ImageCleanupTaskRepository cleanupTaskRepository;
    private final Clock clock;

    @Value("${cleanup.s3-image.grace-period-minutes:60}")
    private long gracePeriodMinutes;

    @Value("${cleanup.s3-image.max-attempt:5}")
    private int maxAttempt;

    @Value("${cleanup.s3-image.max-per-run:500}")
    private int maxPerRun;

    @Bean
    public Job voucherImageCleanupJob(Step voucherImageCleanupStep) {
        return new JobBuilder("voucherImageCleanupJob", jobRepository)
                .start(voucherImageCleanupStep)
                .build();
    }

    @Bean
    public Step voucherImageCleanupStep() {
        return new StepBuilder("voucherImageCleanupStep", jobRepository)
                .tasklet(s3ImageCleanupTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet s3ImageCleanupTasklet() {
        return (contribution, chunkContext) -> {
            LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(gracePeriodMinutes);
            Pageable limit = PageRequest.of(0, maxPerRun, Sort.by(Sort.Direction.ASC, "createdDateTime"));
            List<S3ImageCleanupTask> targets =
                    cleanupTaskRepository.findByCreatedDateTimeBeforeAndAttemptCountLessThan(
                            threshold, maxAttempt, limit);

            for (S3ImageCleanupTask task : targets) {
                cleanupSingleTask(task);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private void cleanupSingleTask(S3ImageCleanupTask task) {
        String objectKey = task.getObjectKey();
        try {
            imageService.deleteImage(objectKey);
            cleanupTaskRepository.delete(task);
            log.info("[S3_CLEANUP] {} {}",
                    kv("event", "cleanup_success"),
                    kv("objectKey", objectKey));
        } catch (Exception e) {
            task.recordFailure(e.getMessage());
            log.warn("[S3_CLEANUP] {} {} {} {}",
                    kv("event", "cleanup_failed"),
                    kv("objectKey", objectKey),
                    kv("attemptCount", task.getAttemptCount()),
                    kv("error", e.getMessage()));
        }
    }
}
