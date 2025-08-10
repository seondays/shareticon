package seondays.shareticon.voucher.batch;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import seondays.shareticon.config.TokenTimeConfig;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

@Configuration
@RequiredArgsConstructor
public class ExpiredVoucherUpdateBatchProcess {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final TokenTimeConfig clock;

    @Bean
    public Job expiredVoucherUpdateJob(Step expiredVoucherUpdateStep) {
        return new JobBuilder("expiredVoucherUpdateJob", jobRepository)
                .start(expiredVoucherUpdateStep)
                .build();
    }

    @Bean
    public Step expiredVoucherUpdateStep(ItemStreamReader<Voucher> expiredVoucherReader) {
        return new StepBuilder("expiredVoucherUpdateStep", jobRepository)
                .<Voucher, Voucher>chunk(10, transactionManager)
                .reader(expiredVoucherReader)
                .processor(expiredVoucherUpdateProcessor())
                .writer(expiredVoucherWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<Voucher> expiredVoucherReader(
            @Value("#{jobParameters['currentDate']}") String currentDateString) {

        LocalDate endOfDay = Optional.ofNullable(currentDateString)
                .map(LocalDate::parse)
                .orElse(LocalDate.now(clock.clock()));
        LocalDate startOfDay = endOfDay.minusDays(1);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startOfDay", startOfDay);
        parameters.put("endOfDay", endOfDay);
        parameters.put("status", VoucherStatus.AVAILABLE);

        return new JpaCursorItemReaderBuilder<Voucher>()
                .name("expiredVoucherReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT v FROM Voucher v WHERE v.isDeleted = false AND v.expiration >= :startOfDay AND v.expiration < :endOfDay AND v.status = :status"
                )
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public ItemProcessor<Voucher, Voucher> expiredVoucherUpdateProcessor() {
        return voucher -> {
            voucher.changeStatusExpired();
            return voucher;
        };
    }

    @Bean
    public JpaItemWriter<Voucher> expiredVoucherWriter() {
        JpaItemWriter<Voucher> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
