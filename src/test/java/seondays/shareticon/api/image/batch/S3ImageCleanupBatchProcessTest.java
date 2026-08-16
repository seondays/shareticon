package seondays.shareticon.api.image.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.business.ImageDeleteException;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.image.S3ImageCleanupTask;
import seondays.shareticon.image.S3ImageCleanupTaskRepository;

public class S3ImageCleanupBatchProcessTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job voucherImageCleanupJob;

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private S3ImageCleanupTaskRepository cleanupTaskRepository;

    @BeforeEach
    void setUp() {
        // now = 2025-05-01T09:00 (KST), grace 60분 → threshold 08:00
        when(clock.instant()).thenReturn(Instant.parse("2025-05-01T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    @AfterEach
    void tearDown() {
        cleanupTaskRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("grace period가 지나고 재시도 한계 미만인 정리 대상만 S3에서 삭제하고 기록을 제거한다")
    void executeCleanupJob() throws Exception {
        //given
        insertTask(1L, "voucher/eligible", 0, LocalDateTime.of(2025, 5, 1, 7, 0));
        insertTask(2L, "voucher/within-grace", 0, LocalDateTime.of(2025, 5, 1, 8, 30));
        insertTask(3L, "voucher/exhausted", 5, LocalDateTime.of(2025, 5, 1, 7, 0));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "cleanup-success")
                .toJobParameters();

        //when
        JobExecution run = jobLauncher.run(voucherImageCleanupJob, jobParameters);

        //then
        assertThat(run.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        verify(imageService, times(1)).deleteImage(eq("voucher/eligible"));
        verify(imageService, never()).deleteImage(eq("voucher/within-grace"));
        verify(imageService, never()).deleteImage(eq("voucher/exhausted"));

        assertThat(cleanupTaskRepository.findByObjectKey("voucher/eligible")).isEmpty();
        assertThat(cleanupTaskRepository.findByObjectKey("voucher/within-grace")).isPresent();
        assertThat(cleanupTaskRepository.findByObjectKey("voucher/exhausted")).isPresent();
    }

    @Test
    @DisplayName("S3 삭제가 실패하면 기록을 유지하고 시도 횟수를 증가시킨다")
    void recordsFailureOnDeleteError() throws Exception {
        //given
        insertTask(1L, "voucher/fail", 0, LocalDateTime.of(2025, 5, 1, 7, 0));

        doThrow(new ImageDeleteException("voucher/fail"))
                .when(imageService).deleteImage("voucher/fail");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "cleanup-fail")
                .toJobParameters();

        //when
        jobLauncher.run(voucherImageCleanupJob, jobParameters);

        //then
        Optional<S3ImageCleanupTask> task = cleanupTaskRepository.findByObjectKey("voucher/fail");
        assertThat(task).isPresent();
        assertThat(task.get().getAttemptCount()).isEqualTo(1);
    }

    private void insertTask(Long id, String objectKey, int attemptCount,
            LocalDateTime createdDateTime) {
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("""
                            INSERT INTO s3_image_cleanup_task
                                (id, object_key, attempt_count, created_date_time, modified_date_time)
                            VALUES (:id, :objectKey, :attemptCount, :createdDateTime, :createdDateTime)
                            """)
                    .setParameter("id", id)
                    .setParameter("objectKey", objectKey)
                    .setParameter("attemptCount", attemptCount)
                    .setParameter("createdDateTime", Timestamp.valueOf(createdDateTime))
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
            return null;
        });
    }
}
