package seondays.shareticon.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.voucher.Voucher;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3ImageOperation imageOperation;

    public String uploadImageWithRetry(VoucherImage voucherImage) {
        MultipartFile imageFile = voucherImage.getImageFile();
        String uploadTitle = voucherImage.makeUploadTitle("voucher");
        return imageOperation.uploadImageWithRetry(imageFile, uploadTitle);
    }

    public String getPresignedImageUrl(String objectKey, Long expirationMinutes) {
        return imageOperation.getPresignedImageUrl(objectKey, expirationMinutes);
    }

    @Async
    public void deleteImageAsync(Voucher voucher) {
        imageOperation.deleteImageWithRetry(voucher);
    }

    public void deleteImage(Voucher voucher) {
        imageOperation.deleteImageWithRetry(voucher);
    }

}
