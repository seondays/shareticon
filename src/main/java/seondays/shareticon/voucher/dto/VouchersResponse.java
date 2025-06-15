package seondays.shareticon.voucher.dto;

import java.time.LocalDate;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VouchersResponse(Long id,
                               String image,
                               String name,
                               LocalDate expiration,
                               VoucherStatus status) {
    public static VouchersResponse of(Voucher voucher) {
        return new VouchersResponse(
                voucher.getId(),
                voucher.getImage(),
                voucher.getName(),
                voucher.getExpiration(),
                voucher.getStatus()
        );
    }

}
