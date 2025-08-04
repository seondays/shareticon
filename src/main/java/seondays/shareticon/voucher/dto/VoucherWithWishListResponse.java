package seondays.shareticon.voucher.dto;

import java.time.LocalDate;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VoucherWithWishListResponse(
        Long id,
        String name,
        Long registeredUserId,
        String image,
        LocalDate expiration,
        VoucherStatus status,
        boolean isWishList
) {

    public VoucherWithWishListResponse(Voucher voucher, boolean isWishList){
        this(
                voucher.getId(),
                voucher.getName(),
                voucher.getUser().getId(),
                voucher.getImage(),
                voucher.getExpiration(),
                voucher.getStatus(),
                isWishList
        );
    }

    public static VoucherWithWishListResponse of(Voucher voucher, boolean isWishList) {
        return new VoucherWithWishListResponse(voucher, isWishList);
    }
}
