package seondays.shareticon.utils.validator.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record VoucherCreationValidationRequest(Long targetUserId,
                                               Long targetGroupId,
                                               LocalDate expiration) {

    public static VoucherCreationValidationRequest of(Long targetUserId, Long targetGroupId, LocalDate expiration) {
        return VoucherCreationValidationRequest
                .builder()
                .targetUserId(targetUserId)
                .targetGroupId(targetGroupId)
                .expiration(expiration)
                .build();
    }
}
