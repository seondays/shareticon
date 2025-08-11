package seondays.shareticon.api.voucher.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static seondays.shareticon.voucher.VoucherStatus.AVAILABLE;
import static seondays.shareticon.voucher.VoucherStatus.USED;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherStatus;

public class ExpiredVoucherUpdateBatchTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private Job expiredVoucherUpdateJob;

    @BeforeEach
    void setUp() {
        Instant systemTime = Instant.parse("2025-05-01T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        when(clock.instant()).thenReturn(systemTime);
        when(clock.getZone()).thenReturn(zoneId);

    }

    @AfterEach
    void tearDown() {
        voucherRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Job은 전날이 만료일인 쿠폰들의 상태를 만료로 변경한다")
    void ExpiredVoucherUpdateJob() throws Exception {
        //given
        User user = createUser();
        Group group = createGroup(user);
        LocalDate now = LocalDate.now(clock);

        Voucher expectedToBeExpired = createVoucher(user, group, now.minusDays(1), AVAILABLE);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "test")
                .addString("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .toJobParameters();

        //when
        JobExecution run = jobLauncher.run(expiredVoucherUpdateJob, jobParameters);

        //then
        assertThat(run.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Voucher updateVoucher = voucherRepository.findById(expectedToBeExpired.getId())
                .orElseThrow();
        assertThat(updateVoucher.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
    }

    @Test
    @DisplayName("만료일이 실행 시점 전날이 아닌 쿠폰은 Job에 의해 변경되지 않는다")
    void ExpiredVoucherUpdateJobWithNotExpired() throws Exception {
        //given
        User user = createUser();
        Group group = createGroup(user);
        LocalDate now = LocalDate.now(clock);

        Voucher expectedToBeAvailable = createVoucher(user, group, now.plusDays(1), AVAILABLE);
        Voucher expectedToBeAvailable2 = createVoucher(user, group, now, AVAILABLE);
        Voucher expectedToBeExpired = createVoucher(user, group, now.minusDays(1), AVAILABLE);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "test2")
                .addString("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .toJobParameters();

        //when
        JobExecution run = jobLauncher.run(expiredVoucherUpdateJob, jobParameters);

        //then
        assertThat(run.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Voucher updateAvailableVoucher = voucherRepository.findById(expectedToBeAvailable.getId())
                .orElseThrow();
        Voucher updateAvailableVoucher2 = voucherRepository.findById(expectedToBeAvailable2.getId())
                .orElseThrow();
        Voucher updateExpiredVoucher = voucherRepository.findById(expectedToBeExpired.getId())
                .orElseThrow();

        assertThat(updateAvailableVoucher.getStatus()).isEqualTo(AVAILABLE);
        assertThat(updateAvailableVoucher2.getStatus()).isEqualTo(AVAILABLE);
        assertThat(updateExpiredVoucher.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 Job에 의해 상태가 변경되지 않는다")
    void ExpiredVoucherUpdateJobWithUsedStatus() throws Exception {
        //given
        User user = createUser();
        Group group = createGroup(user);
        LocalDate now = LocalDate.now(clock);

        Voucher expectedToBeMaintainState = createVoucher(user, group, now.plusDays(1), USED);
        Voucher expectedToBeExpired = createVoucher(user, group, now.minusDays(1), AVAILABLE);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "test3")
                .addString("currentDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .toJobParameters();

        //when
        JobExecution run = jobLauncher.run(expiredVoucherUpdateJob, jobParameters);

        //then
        assertThat(run.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Voucher updateMaintainState = voucherRepository.findById(expectedToBeMaintainState.getId())
                .orElseThrow();
        Voucher updateExpiredVoucher = voucherRepository.findById(expectedToBeExpired.getId())
                .orElseThrow();

        assertThat(updateMaintainState.getStatus()).isEqualTo(USED);
        assertThat(updateExpiredVoucher.getStatus()).isEqualTo(VoucherStatus.EXPIRED);

    }

    private User createUser() {
        User user = User.builder().build();
        return userRepository.save(user);
    }

    private Group createGroup(User user) {
        Group group = Group.builder().leaderUser(user).build();
        return groupRepository.save(group);
    }

    private Voucher createVoucher(User user, Group group, LocalDate expiration,
            VoucherStatus status) {
        Voucher voucher = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(status).isDeleted(false).build();
        return voucherRepository.save(voucher);
    }

}
