package seondays.shareticon.voucher.dto;

import jakarta.validation.constraints.NotNull;

public record CreateVoucherRequest(
        @NotNull(message = "그룹 ID를 포함해야 합니다")
        Long groupId) {
}
