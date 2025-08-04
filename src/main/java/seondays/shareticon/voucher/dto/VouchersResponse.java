package seondays.shareticon.voucher.dto;

import java.time.LocalDate;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VouchersResponse(Long id,
                               String presignedImage,
                               String name,
                               Long registeredUserId,
                               LocalDate expiration,
                               VoucherStatus status,
                               boolean isWishList) {
    public static VouchersResponse of(VoucherWithWishListResponse voucher, String presignedImage) {
        return new VouchersResponse(
                voucher.id(),
                presignedImage,
                voucher.name(),
                voucher.registeredUserId(),
                voucher.expiration(),
                voucher.status(),
                voucher.isWishList()
        );
    }

    public static VouchersResponse withWishList(Voucher voucher, String presignedImage, boolean isWishList) {
        return new VouchersResponse(
                voucher.getId(),
                presignedImage,
                voucher.getName(),
                voucher.getUser().getId(),
                voucher.getExpiration(),
                voucher.getStatus(),
                isWishList
        );
    }

}
