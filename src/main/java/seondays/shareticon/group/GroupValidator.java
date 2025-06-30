package seondays.shareticon.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.InvalidAcceptGroupJoinApplyException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.dto.GroupJoinApplyStatusChangeValidationRequest;
import seondays.shareticon.group.dto.GroupTitleAliasChangeValidationRequest;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroupRepository;

@Component
@RequiredArgsConstructor
public class GroupValidator {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;

    public void validateGroupTitleAliasChange(GroupTitleAliasChangeValidationRequest request) {
        validateUserAndGroupExist(request.requestUserId(), request.targetGroupId());
    }

    public void validateGroupJoinApplyStatusChange(GroupJoinApplyStatusChangeValidationRequest request) {
        validateLeader(request.leaderId(), request.targetGroup());
        validateExistTargetUser(request.targetUserId());
    }

    public void validateExistTargetUser(Long targetUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new UserNotFoundException();
        }
    }

    private void validateLeader(Long leaderId, Group group) {
        if (userRepository.findById(leaderId).isEmpty()) {
            throw new UserNotFoundException();
        }
        if (!leaderId.equals(group.getLeaderUser().getId())) {
            throw new InvalidAcceptGroupJoinApplyException();
        }
        if (!userGroupRepository.existsByUserIdAndGroupId(leaderId, group.getId())) {
            throw new InvalidAcceptGroupJoinApplyException();
        }
    }

    private void validateUserAndGroupExist(Long userId, Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException();
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
    }

}
