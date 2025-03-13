package seondays.shareticon.image;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.ImageUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageService {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucket;

    public ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

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

            URL url = s3Client.utilities().getUrl(builder -> builder
                    .bucket(bucket)
                    .key(uploadTitle)
                    .build());

            return url.toString();
        } catch (IOException e) {
            throw new ImageUploadException();
        }
    }

    public String makeUploadTitle(String prefix, String filename) {
        return prefix + "/" + UUID.randomUUID() + "-" + filename;
    }
}
