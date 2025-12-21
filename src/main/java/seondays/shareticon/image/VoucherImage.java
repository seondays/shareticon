package seondays.shareticon.image;

import java.util.UUID;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.business.IllegalVoucherImageException;

@Getter
public class VoucherImage {

    private final MultipartFile imageFile;

    public VoucherImage(MultipartFile imageFile) {
        this.imageFile = imageFile;
        validateImageFile();
    }

    public static VoucherImage of(MultipartFile imageFile) {
        return new VoucherImage(imageFile);
    }

    private void validateImageFile() {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalVoucherImageException();
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new IllegalVoucherImageException();
        }
    }

    public String makeUploadTitle() {
        String prefix = "voucher";
        return prefix + "/" + UUID.randomUUID();
    }
}
