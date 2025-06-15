package seondays.shareticon.voucher.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateVoucherRequest(
        @NotNull(message = "그룹 ID를 포함해야 합니다")
        Long groupId,
        @NotEmpty(message = "쿠폰 이름을 포함해야 합니다")
        String voucherName,
        @NotNull(message = "쿠폰의 만료 일자를 포함해야 합니다")
        LocalDate expiration) {

}
