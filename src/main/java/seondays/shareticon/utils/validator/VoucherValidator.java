package seondays.shareticon.utils.validator;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.business.InvalidAccessException;
import seondays.shareticon.exception.business.InvalidVoucherDeleteException;
import seondays.shareticon.exception.business.InvalidVoucherExpireException;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.utils.validator.dto.VoucherCreationValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherDeletionValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherAccessValidationRequest;

@Component
@RequiredArgsConstructor
public class VoucherValidator {

    private final BasicValidator basicValidator;
    private final Clock clock;

    public void validateVoucherCreation(VoucherCreationValidationRequest request) {
        Long targetUserId = request.targetUserId();
        Long targetGroupId = request.targetGroupId();

        basicValidator.validateJoinedGroupMember(targetUserId, targetGroupId);
        validateVoucherDateExpiration(request.expiration());
    }

    public void validateVoucherDeletion(VoucherDeletionValidationRequest request) {
        Long targetUserId = request.targetUserId();
        Long targetGroupId = request.targetGroupId();
        Voucher targetVoucher = request.targetVoucher();

        basicValidator.validateVoucherExist(targetVoucher.getId());
        basicValidator.validateJoinedGroupMember(targetUserId, targetGroupId);

        validateVoucherOwner(targetUserId, targetVoucher);
        validateVoucherInGroup(targetGroupId, targetVoucher);
    }

    public void validateAccessVoucher(VoucherAccessValidationRequest request) {
        basicValidator.validateVoucherExist(request.targetVoucherId());
        basicValidator.validateJoinedGroupMember(request.targetUserId(), request.targetGroupId());
    }

    private void validateVoucherInGroup(Long expectedGroupId, Voucher voucher) {
        Long voucherGroupId = voucher.getGroup().getId();
        Long voucherId = voucher.getId();

        basicValidator.validateVoucherExist(voucherId);

        if (!voucherGroupId.equals(expectedGroupId)) {
            throw InvalidAccessException.of(voucherGroupId);
        }
    }

    private void validateVoucherOwner(Long userId, Voucher voucher) {
        Long voucherOwnerUserId = voucher.getUser().getId();
        Long voucherId = voucher.getId();

        basicValidator.validateVoucherExist(voucherId);

        if (!voucherOwnerUserId.equals(userId)) {
            throw InvalidVoucherDeleteException.of(voucherId);
        }
    }

    private void validateVoucherDateExpiration(LocalDate expiration) {
        LocalDate today = LocalDate.now(clock);
        if(today.isAfter(expiration)) {
            throw InvalidVoucherExpireException.of(expiration);
        }
    }

}
