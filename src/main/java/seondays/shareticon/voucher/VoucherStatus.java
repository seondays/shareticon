package seondays.shareticon.voucher;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VoucherStatus {
    USED("사용 완료한 쿠폰"),
    AVAILABLE("사용 가능한 쿠폰"),
    EXPIRED("기간이 만료된 쿠폰");

    private final String discription;

    public static List<VoucherStatus> forDisplayVoucherStatus() {
        return List.of(USED, AVAILABLE);
    }
}
