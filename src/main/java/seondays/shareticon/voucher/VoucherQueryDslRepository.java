package seondays.shareticon.voucher;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seondays.shareticon.voucher.dto.VoucherWithWishListResponse;

public interface VoucherQueryDslRepository {

    Slice<VoucherWithWishListResponse> searchVoucher(Long userId, Long groupId,
            VoucherFilterCondition voucherFilterCondition, Long cursorId, Pageable pageable);
}
