package seondays.shareticon.voucher.dto;

import lombok.Builder;

@Builder
public record VoucherStatusChangeValidationRequest(Long userId,
                                                   Long groupId) {

}
