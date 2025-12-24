package seondays.shareticon.image;

import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.business.ImageUploadException;
import seondays.shareticon.exception.business.PresignedUrlGenerationException;
import seondays.shareticon.voucher.Voucher;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageOperation {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Retryable(
            retryFor = {SdkClientException.class},
            noRetryFor = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delayExpression = "${retry.S3-Image-service.delay}", multiplier = 2)
    )
    public String uploadImageWithRetry(MultipartFile image, String uploadTitle) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(uploadTitle)
                    .contentType(image.getContentType())
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(image.getInputStream(),
                    image.getSize());

            s3Client.putObject(putObjectRequest, requestBody);

            return uploadTitle;
        } catch (IOException e) {
            throw ImageUploadException.of(uploadTitle);
        }
    }

    @Recover
    public String recoverImageUpload(Exception e, MultipartFile image, String uploadTitle) {
        throw ImageUploadException.of(uploadTitle);
    }

    @Retryable(
            retryFor = {SdkClientException.class},
            noRetryFor = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delayExpression = "${retry.S3-Image-service.delay}", multiplier = 2)
    )
    public void deleteImageWithRetry(Voucher voucher) {
        String key = voucher.getImage();

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    @Recover
    public void recoverImageDelete(Exception e, Voucher voucher) {
        log.error("[S3] -- {}번 쿠폰의 {} 이미지 삭제 시도 3회가 모두 실패했습니다", voucher.getId(), voucher.getImage(), e);
    }

    @Cacheable(
            cacheNames = "voucherImage",
            key = "#objectKey"
    )
    public String getPresignedImageUrl(String objectKey, Long expirationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignedUrlGenerationRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            PresignedGetObjectRequest generatedPresignedUrl = s3Presigner.presignGetObject(
                    presignedUrlGenerationRequest);
            return generatedPresignedUrl.url().toString();
        } catch (Exception e) {
            throw PresignedUrlGenerationException.of(objectKey);
        }
    }
}
