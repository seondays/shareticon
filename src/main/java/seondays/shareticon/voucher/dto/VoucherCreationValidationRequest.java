package seondays.shareticon.voucher.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record VoucherCreationValidationRequest(Long userId,
                                               Long groupId,
                                               LocalDate expiration) {

}
