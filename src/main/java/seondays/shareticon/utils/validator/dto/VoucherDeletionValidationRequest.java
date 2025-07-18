package seondays.shareticon.utils.validator.dto;

import lombok.Builder;
import seondays.shareticon.voucher.Voucher;

@Builder
public record VoucherDeletionValidationRequest(Long targetUserId,
                                               Long targetGroupId,
                                               Voucher targetVoucher) {

    public static VoucherDeletionValidationRequest of(Long targetUserId, Long targetGroupId, Voucher targetVoucher) {
        return VoucherDeletionValidationRequest.builder()
                .targetUserId(targetUserId)
                .targetGroupId(targetGroupId)
                .targetVoucher(targetVoucher)
                .build();
    }
}
