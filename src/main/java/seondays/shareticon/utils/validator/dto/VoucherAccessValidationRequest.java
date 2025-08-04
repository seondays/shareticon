package seondays.shareticon.utils.validator.dto;

import lombok.Builder;

@Builder
public record VoucherAccessValidationRequest(Long targetUserId,
                                             Long targetGroupId,
                                             Long targetVoucherId) {

    public static VoucherAccessValidationRequest of(Long targetUserId, Long targetGroupId, Long targetVoucherId) {
        return VoucherAccessValidationRequest.builder()
                .targetUserId(targetUserId)
                .targetGroupId(targetGroupId)
                .targetVoucherId(targetVoucherId)
                .build();
    }
}
