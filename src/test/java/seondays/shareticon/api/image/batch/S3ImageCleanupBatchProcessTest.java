package seondays.shareticon.api.image.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.voucher.Voucher;

public class S3ImageCleanupBatchProcessTest extends IntegrationTestSupport {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job voucherImageCleanupJob;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        Instant systemTime = Instant.parse("2025-05-01T00:00:00Z");
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        when(clock.instant()).thenReturn(systemTime);
        when(clock.getZone()).thenReturn(zoneId);

    }

    @Test
    @DisplayName("배치 실행 날짜 2달 이내로 삭제된 상태인 쿠폰들을 대상으로 배치가 실행된다")
    void executeVoucherCleanupJob() throws Exception {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().leaderUser(user).build();
        groupRepository.save(group);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime testDateTime = now.minusMonths(2);

        Voucher expectedToBeDeleted = createDeletedVoucher(user, group, 1L, testDateTime);
        Voucher expectedToBeSkipped = createDeletedVoucher(user, group, 2L, testDateTime.minusDays(1));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", "test")
                .addString("executionDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .toJobParameters();

        //when
        JobExecution run = jobLauncher.run(voucherImageCleanupJob, jobParameters);

        //then
        assertThat(run.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(imageService, times(1)).deleteImage(any());
        verify(imageService).deleteImage(argThat(voucher ->
                voucher.getId().equals(expectedToBeDeleted.getId())));
        verify(imageService, never()).deleteImage(argThat(voucher ->
                voucher.getId().equals(expectedToBeSkipped.getId())));

    }

    public Voucher createDeletedVoucher(User user, Group group, Long voucherId,
            LocalDateTime modifiedDateTime) {
        // auditing 우회를 위해 쿼리 직접 날려서 저장하도록 설정함
        return transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("""
                                INSERT INTO voucher (id, user_id, group_id, is_deleted, modified_date_time)
                                VALUES (:id, :userId, :groupId, true, :modifiedDateTime)
                            """)
                    .setParameter("id", voucherId)
                    .setParameter("userId", user.getId())
                    .setParameter("groupId", group.getId())
                    .setParameter("modifiedDateTime", Timestamp.valueOf(modifiedDateTime))
                    .executeUpdate();

            entityManager.flush();
            entityManager.clear();
            return entityManager.find(Voucher.class, voucherId);
        });
    }
}