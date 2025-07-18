package seondays.shareticon.utils.validator.dto;

import lombok.Builder;
import seondays.shareticon.voucher.Voucher;

@Builder
public record VoucherStatusChangeValidationRequest(Long targetUserId,
                                                   Long targetGroupId,
                                                   Long targetVoucherId) {

    public static VoucherStatusChangeValidationRequest of(Long targetUserId, Long targetGroupId, Long targetVoucherId) {
        return VoucherStatusChangeValidationRequest.builder()
                .targetUserId(targetUserId)
                .targetGroupId(targetGroupId)
                .targetVoucherId(targetVoucherId)
                .build();
    }
}
