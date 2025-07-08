package seondays.shareticon.image.batch;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import seondays.shareticon.config.TokenTimeConfig;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.voucher.Voucher;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3ImageCleanupBatchProcess {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final ImageService imageService;
    private final TokenTimeConfig clock;

    @Bean
    public Job voucherImageCleanupJob(Step voucherImageCleanupStep) {
        return new JobBuilder("voucherImageCleanupJob", jobRepository)
                .start(voucherImageCleanupStep)
                .build();
    }

    @Bean
    public Step voucherImageCleanupStep(ItemReader<Voucher> voucherReader) {
        return new StepBuilder("voucherImageCleanupStep", jobRepository)
                .<Voucher, Voucher>chunk(10, transactionManager)
                .reader(voucherReader)
                .processor(voucherImageCleanupProcessor())
                .writer(voucherWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<Voucher> voucherReader(
            @Value("#{jobParameters['executionDate']}") String executionDateString) {
        LocalDateTime executionDateTime = Optional.ofNullable(executionDateString)
                .map(LocalDateTime::parse)
                .orElse(LocalDateTime.now(clock.clock()));

        LocalDateTime executionScope = executionDateTime.minusMonths(2);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("executionScope", executionScope);

        return new JpaPagingItemReaderBuilder<Voucher>()
                .name("voucherReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT v FROM Voucher v WHERE v.isDeleted = true AND v.modifiedDateTime >= :executionScope")
                .parameterValues(parameters)
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Voucher, Voucher> voucherImageCleanupProcessor() {
        return voucher -> {
            try {
                imageService.deleteImageWithRetry(voucher);
                return voucher;
            } catch (Exception e) {
                log.warn("배치 처리 중 이미지 삭제 실패 : Voucher {}", voucher.getId());
                return null;
            }
        };
    }

    @Bean
    public ItemWriter<Voucher> voucherWriter() {
        return items -> {

        };
    }

}
