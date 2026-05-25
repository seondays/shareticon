package seondays.shareticon.warmup;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.VoucherFilterCondition;
import seondays.shareticon.voucher.VoucherService;
import seondays.shareticon.voucher.dto.VoucherListResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarmupService {

    private final VoucherService voucherService;
    private final WarmupProperties warmupProperties;

    @PostConstruct
    void validate() {
        if (!warmupProperties.hasTarget()) {
            log.warn("warmup.user-id or warmup.group-id is not configured. Warmup request will fail.");
        }
    }

    public WarmupResult warmupVoucherList() {
        if (!warmupProperties.hasTarget()) {
            throw new IllegalStateException("Warmup target userId and groupId must be configured");
        }

        long startTime = System.nanoTime();

        Long userId = warmupProperties.userId();
        Long groupId = warmupProperties.groupId();
        int pageSize = warmupProperties.actualPageSize();

        SliceResponse<VoucherListResponse> response = voucherService.getAllVoucher(
                userId,
                groupId,
                null,
                pageSize,
                VoucherFilterCondition.of(null, null, null)
        );

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        int warmedVoucherCount = countWarmedVouchers(response.getContent());

        return new WarmupResult(
                userId,
                groupId,
                pageSize,
                warmedVoucherCount,
                response.hasNext(),
                durationMs
        );
    }

    private int countWarmedVouchers(List<VoucherListResponse> response) {
        if (response.isEmpty()) {
            return 0;
        }
        return response.get(0).vouchers().size();
    }
}
