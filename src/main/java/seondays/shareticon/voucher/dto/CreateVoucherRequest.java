package seondays.shareticon.voucher.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateVoucherRequest(
        @NotNull(message = "그룹 ID를 포함해야 합니다")
        Long groupId,
        @NotEmpty(message = "쿠폰 이름을 포함해야 합니다")
        String voucherName,
        @NotNull(message = "쿠폰의 만료 일자를 포함해야 합니다")
        @FutureOrPresent(message = "쿠폰의 만료 일자는 오늘보다 이전 날짜로 지정할 수 없습니다")
        LocalDate expiration) {

}
