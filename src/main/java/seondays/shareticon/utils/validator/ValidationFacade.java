package seondays.shareticon.utils.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.utils.validator.dto.GroupJoinApplyStatusChangeValidationRequest;
import seondays.shareticon.utils.validator.dto.LeaderIdAndGroupsValidationRequest;
import seondays.shareticon.utils.validator.dto.UserIdAndGroupIdValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherCreationValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherDeletionValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherStatusChangeValidationRequest;

@Component
@RequiredArgsConstructor
public class ValidationFacade {

    private final UserValidator userValidator;
    private final GroupValidator groupValidator;
    private final VoucherValidator voucherValidator;

    public void validateGroupJoinApplyStatusChange(GroupJoinApplyStatusChangeValidationRequest request) {
        groupValidator.validateGroupJoinApplyStatusChange(request);
    }

    public void validateGroupTitleAliasChange(UserIdAndGroupIdValidationRequest request) {
        groupValidator.validateGroupTitleAliasChange(request);
    }

    public void validateLeaderForPendingUserList(LeaderIdAndGroupsValidationRequest request) {
        groupValidator.validateLeaderForPendingUserList(request);
    }

    public void validateVoucherCreation(VoucherCreationValidationRequest request) {
        voucherValidator.validateVoucherCreation(request);
    }

    public void validateVoucherDeletion(VoucherDeletionValidationRequest request) {
        voucherValidator.validateVoucherDeletion(request);
    }

    public void validateVoucherStatusChange(VoucherStatusChangeValidationRequest request) {
        voucherValidator.validateVoucherStatusChange(request);
    }

    public void validateUserProfile(String nickName) {
        userValidator.validateUserProfile(nickName);
    }

}
