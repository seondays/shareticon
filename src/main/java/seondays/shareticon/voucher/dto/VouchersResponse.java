package seondays.shareticon.voucher.dto;

import java.time.LocalDate;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VouchersResponse(Long id,
                               String presignedImage,
                               String name,
                               Long registeredUserId,
                               LocalDate expiration,
                               VoucherStatus status) {
    public static VouchersResponse of(Voucher voucher, String presignedImage) {
        return new VouchersResponse(
                voucher.getId(),
                presignedImage,
                voucher.getName(),
                voucher.getUser().getId(),
                voucher.getExpiration(),
                voucher.getStatus()
        );
    }

}
