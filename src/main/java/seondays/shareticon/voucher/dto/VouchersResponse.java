package seondays.shareticon.voucher.dto;

import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VouchersResponse(Long id,
                               String image,
                               VoucherStatus status) {
    public static VouchersResponse of(Voucher voucher) {
        return new VouchersResponse(
                voucher.getId(),
                voucher.getImage(),
                voucher.getStatus()
        );
    }

}
