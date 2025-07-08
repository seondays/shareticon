package seondays.shareticon.api.image.batch;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import seondays.shareticon.image.batch.S3ImageCleanupBatchScheduler;

@ExtendWith(MockitoExtension.class)
class S3ImageCleanupBatchSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job voucherImageCleanupJob;

    @Mock
    private Clock clock;

    private S3ImageCleanupBatchScheduler s3ImageCleanupBatchScheduler;

    @BeforeEach
    void setUp() {
        s3ImageCleanupBatchScheduler = new S3ImageCleanupBatchScheduler(jobLauncher, voucherImageCleanupJob, clock);

        Instant systemTime = Instant.parse("2025-05-01T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        when(clock.instant()).thenReturn(systemTime);
        when(clock.getZone()).thenReturn(zoneId);

    }

    @Test
    @DisplayName("배치 작업이 정상적으로 트리거된다")
    void triggerBatchSchedulerJob()
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        when(jobLauncher.run(any(), any())).thenReturn(new JobExecution(1L));

        s3ImageCleanupBatchScheduler.runVoucherImageCleanupJob();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(voucherImageCleanupJob), captor.capture());

        JobParameters params = captor.getValue();
        assertThat(params.getString("jobId")).isEqualTo("20250501");
    }

}
