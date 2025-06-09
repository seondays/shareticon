package seondays.shareticon.voucher.dto;

import java.util.List;
import seondays.shareticon.userGroup.UserGroup;

public record VoucherListResponse(Long groupId,
                                  String groupTitle,
                                  List<VouchersResponse> vouchers) {

    public static VoucherListResponse of(List<VouchersResponse> vouchers, UserGroup userGroup) {
        return new VoucherListResponse(
                userGroup.getGroup().getId(),
                userGroup.getGroupTitleAlias(),
                vouchers
                );
    }

}
