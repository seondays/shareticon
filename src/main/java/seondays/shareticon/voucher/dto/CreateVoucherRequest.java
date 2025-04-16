package seondays.shareticon.voucher.dto;

import jakarta.validation.constraints.NotEmpty;

public record CreateVoucherRequest(
        @NotEmpty(message = "그룹 ID를 포함해야 합니다")
        Long groupId) {
}
