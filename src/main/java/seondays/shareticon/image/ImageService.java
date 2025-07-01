package seondays.shareticon.image;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.ImageDeleteException;
import seondays.shareticon.exception.ImageUploadException;
import seondays.shareticon.exception.PresignedUrlGenerationException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile image) {
        String uploadTitle = makeUploadTitle("voucher", image.getOriginalFilename());

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
            throw new ImageUploadException();
        }
    }

    public String makeUploadTitle(String prefix, String filename) {
        return prefix + "/" + UUID.randomUUID() + "-" + filename;
    }

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
            throw new PresignedUrlGenerationException();
        }
    }

    public void deleteImage(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new ImageDeleteException();
        }
    }
}
