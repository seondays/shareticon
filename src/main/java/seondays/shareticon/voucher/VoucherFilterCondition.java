package seondays.shareticon.voucher;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record VoucherFilterCondition(List<VoucherStatus> voucherStatuses,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDay,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDay) {

    public static VoucherFilterCondition of(List<VoucherStatus> voucherStatuses, LocalDate startDay, LocalDate endDay) {
        return new VoucherFilterCondition(voucherStatuses, startDay, endDay);
    }

}
