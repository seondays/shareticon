package seondays.shareticon.api.voucher.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
import org.springframework.batch.core.launch.JobLauncher;
import seondays.shareticon.voucher.batch.ExpiredVoucherUpdateBatchScheduler;

@ExtendWith(MockitoExtension.class)
public class ExpiredVoucherUpdateBatchSchedulerTest {

    private ExpiredVoucherUpdateBatchScheduler expiredVoucherUpdateBatchScheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Clock clock;

    @Mock
    private Job expiredVoucherUpdateJob;

    @BeforeEach
    void setUp() {
        expiredVoucherUpdateBatchScheduler = new ExpiredVoucherUpdateBatchScheduler(jobLauncher, clock, expiredVoucherUpdateJob);

        Instant systemTime = Instant.parse("2025-05-01T00:00:00Z");
        when(clock.instant()).thenReturn(systemTime);
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        when(clock.getZone()).thenReturn(zoneId);

    }

    @Test
    @DisplayName("배치 작업이 매일 00시에 정상적으로 트리거된다")
    void triggerBatchSchedulerJob() throws Exception {
        //given
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(1L));

        //when
        expiredVoucherUpdateBatchScheduler.runExpiredVoucherUpdateJob();

        //then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(1)).run(eq(expiredVoucherUpdateJob), captor.capture());

        JobParameters params = captor.getValue();
        assertThat(params.getString("jobId")).isEqualTo("runExpiredVoucherUpdateJob20250501");
        assertThat(params.getString("currentDate")).isEqualTo("2025-05-01");

    }

}
