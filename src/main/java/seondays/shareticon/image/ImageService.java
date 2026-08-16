package seondays.shareticon.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3ImageOperation imageOperation;

    public void uploadImage(VoucherImage voucherImage, String objectKey) {
        MultipartFile imageFile = voucherImage.getImageFile();
        imageOperation.uploadImageWithRetry(imageFile, objectKey);
    }

    public String getPresignedImageUrl(String objectKey, Long expirationMinutes) {
        return imageOperation.getPresignedImageUrl(objectKey, expirationMinutes);
    }

    public void deleteImage(String objectKey) {
        imageOperation.deleteImageWithRetry(objectKey);
    }
}
