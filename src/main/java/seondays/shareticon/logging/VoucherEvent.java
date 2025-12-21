package seondays.shareticon.logging;

import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;

public record VoucherEvent(
        Long voucherId,
        Long groupId,
        Actions action
) {

    public static VoucherEvent toDelete(Long voucherId, Long groupId) {
        return new VoucherEvent(voucherId, groupId, Actions.DELETE);
    }

    public static VoucherEvent toRegister(Long voucherId, Long groupId) {
        return new VoucherEvent(voucherId, groupId, Actions.REGISTER);
    }

    public static VoucherEvent toChangeStatus(Voucher voucher, Long groupId) {
        Actions actions = null;
        if (voucher.getStatus() == VoucherStatus.AVAILABLE) {
            actions = Actions.MAKE_AVAILABLE;
        } else if (voucher.getStatus() == VoucherStatus.USED) {
            actions = Actions.MAKE_USED;
        }
        return new VoucherEvent(voucher.getId(), groupId, actions);
    }
}
