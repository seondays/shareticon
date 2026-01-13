package seondays.shareticon.utils.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.business.InvalidGroupLeaderException;
import seondays.shareticon.group.Group;
import seondays.shareticon.utils.validator.dto.GroupJoinApplyStatusChangeValidationRequest;
import seondays.shareticon.utils.validator.dto.LeaderIdAndGroupsValidationRequest;
import seondays.shareticon.utils.validator.dto.UserIdAndGroupIdValidationRequest;

@Component
@RequiredArgsConstructor
public class GroupValidator {

    private final BasicValidator basicValidator;

    public void validateGroupTitleAliasChange(UserIdAndGroupIdValidationRequest request) {
        basicValidator.validateUserExist(request.targetUserId());
        basicValidator.validateGroupExist(request.targetGroupId());
    }

    public void validateGroupJoinApplyStatusChange(GroupJoinApplyStatusChangeValidationRequest request) {
        Long leaderId = request.leaderId();

        basicValidator.validateJoinedGroupMember(leaderId, request.targetGroup().getId());
        basicValidator.validateUserExist(request.targetUserId());
        validateGroupLeader(leaderId, request.targetGroup());
    }

    public void validateLeaderForPendingUserList(LeaderIdAndGroupsValidationRequest request) {
        Long leaderId = request.leaderId();

        basicValidator.validateUserExist(leaderId);
        for (Group g : request.targetGroups()) {
            basicValidator.validateJoinedGroupMember(leaderId, g.getId());
            validateGroupLeader(leaderId, g);
        }
    }

    public void validateGroupLeader(Long userId, Group group) {
        Long leaderId = group.getLeaderUser().getId();

        if (!userId.equals(leaderId)) {
            throw new InvalidGroupLeaderException(group.getId());
        }
    }

}
