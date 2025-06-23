package seondays.shareticon.voucher.dto;

import lombok.Builder;
import seondays.shareticon.voucher.Voucher;

@Builder
public record VoucherDeletionValidationRequest(Long userId,
                                               Long groupId,
                                               Voucher voucher) {

}
