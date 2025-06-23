package seondays.shareticon.voucher;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.InvalidAccessVoucherException;
import seondays.shareticon.exception.InvalidVoucherDeleteException;
import seondays.shareticon.exception.InvalidVoucherExpireException;
import seondays.shareticon.user.User;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.dto.VoucherCreationValidationRequest;
import seondays.shareticon.voucher.dto.VoucherDeletionValidationRequest;
import seondays.shareticon.voucher.dto.VoucherStatusChangeValidationRequest;

@Component
@RequiredArgsConstructor
public class VoucherValidator {

    private final UserGroupRepository userGroupRepository;
    private final Clock clock;

    public void validateVoucherCreation(VoucherCreationValidationRequest request) {
        validateUserInGroup(request.userId(), request.groupId());
        validateVoucherDateExpiration(request.expiration());
    }

    public void validateVoucherDeletion(VoucherDeletionValidationRequest request) {
        User voucherOwnerUser = request.voucher().getUser();
        Long targetUserId = request.userId();
        Long targetGroupId = request.groupId();

        validateVoucherOwner(targetUserId, voucherOwnerUser);
        validateUserAndVoucherInGroup(targetUserId, targetGroupId, request.voucher());
    }

    public void validateVoucherStatusChange(VoucherStatusChangeValidationRequest request) {
        validateUserInGroup(request.userId(), request.groupId());
    }

    private void validateUserAndVoucherInGroup(Long userId, Long groupId, Voucher voucher) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidVoucherDeleteException();
        }
        if (!voucher.getGroup().getId().equals(groupId)) {
            throw new InvalidVoucherDeleteException();
        }
    }

    private void validateUserInGroup(Long userId, Long groupId) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }
    }

    private void validateVoucherOwner(Long userId, User voucherUser) {
        if (!voucherUser.getId().equals(userId)) {
            throw new InvalidVoucherDeleteException();
        }
    }

    private void validateVoucherDateExpiration(LocalDate expiration) {
        LocalDate today = LocalDate.now(clock);
        if(expiration.isBefore(today)) {
            throw new InvalidVoucherExpireException();
        }
    }

}
