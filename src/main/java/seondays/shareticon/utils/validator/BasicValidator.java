package seondays.shareticon.utils.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.InvalidAccessException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.VoucherRepository;

@Component
@RequiredArgsConstructor
public class BasicValidator {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final VoucherRepository voucherRepository;
    private final UserGroupRepository userGroupRepository;

    public void validateUserExist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
    }

    public void validateGroupExist(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException();
        }
    }

    public void validateVoucherExist(Long voucherId) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new VoucherNotFoundException();
        }
    }

    public void validateUserJoinGroup(Long userId, Long groupId) {
        if (!userGroupRepository.existsByUserIdAndGroupIdAndJoinStatus(userId, groupId, JoinStatus.JOINED)) {
            throw new InvalidAccessException();
        }
    }

    public void validateJoinedGroupMember(Long userId, Long groupId) {
        validateUserExist(userId);
        validateGroupExist(groupId);
        validateUserJoinGroup(userId, groupId);
    }

}
