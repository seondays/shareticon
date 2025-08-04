package seondays.shareticon.wishlist.dto;

import java.time.LocalDate;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.wishlist.WishList;

public record WishListResponse(Long voucherId,
                               String presignedImage,
                               String voucherName,
                               Long groupId,
                               Long registeredUserId,
                               LocalDate expiration,
                               VoucherStatus status) {

    public static WishListResponse of(WishList wishList, String presignedImage) {
        Voucher voucher = wishList.getVoucher();
        return new WishListResponse(
                voucher.getId(),
                presignedImage,
                voucher.getName(),
                wishList.getGroup().getId(),
                voucher.getUser().getId(),
                voucher.getExpiration(),
                voucher.getStatus());
    }
}
