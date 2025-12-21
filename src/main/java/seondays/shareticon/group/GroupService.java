package seondays.shareticon.group;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.business.GroupNotFoundException;
import seondays.shareticon.exception.business.GroupUserNotFoundException;
import seondays.shareticon.exception.business.UserNotFoundException;
import seondays.shareticon.group.dto.ApplyToJoinRequest;
import seondays.shareticon.group.dto.ApplyToJoinResponse;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasRequest;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasResponse;
import seondays.shareticon.group.dto.CreateGroupRequest;
import seondays.shareticon.utils.validator.dto.GroupJoinApplyStatusChangeValidationRequest;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.group.dto.GroupResponse;
import seondays.shareticon.utils.validator.dto.LeaderIdAndGroupsValidationRequest;
import seondays.shareticon.utils.validator.dto.UserIdAndGroupIdValidationRequest;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.utils.validator.ValidationFacade;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupFactory groupFactory;
    private final ValidationFacade validationFacade;

    public GroupResponse registerNewGroup(Long userId, CreateGroupRequest request) {
        User leaderUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return groupFactory.createGroupWithRetry(leaderUser, request);
    }

    @Transactional(readOnly = true)
    public List<GroupListResponse> getAllGroupList(Long userId) {
        return userGroupRepository.findGroupsWithMemberCountByUserIdAndStatus(
                        userId, JoinStatus.getStatusesForAcceptedGroupMembers())
                .stream()
                .toList();
    }

    @Transactional
    public void applyToJoinGroup(ApplyToJoinRequest request, Long userId) {
        String inviteCode = request.inviteCode();
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(GroupNotFoundException::new);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Optional<UserGroup> existingUserGroup = userGroupRepository.findByUserIdAndGroupId(userId,
                group.getId());

        UserGroup userGroup = UserGroup.applyToJoin(user, group, existingUserGroup);
        userGroupRepository.save(userGroup);
    }

    @Transactional(readOnly = true)
    public List<ApplyToJoinResponse> getAllGroupPendingUserList(Long leaderUserId) {
        List<Group> allGroupByLeaderUserId = groupRepository.findAllByLeaderId(leaderUserId);

        LeaderIdAndGroupsValidationRequest request = LeaderIdAndGroupsValidationRequest.of(
                leaderUserId, allGroupByLeaderUserId);
        validationFacade.validateLeaderForPendingUserList(request);

        return allGroupByLeaderUserId.stream()
                .map(group -> ApplyToJoinResponse.of(group.getId(),
                        group.getGroupAlias(leaderUserId), group.getPendingMemberResponses()))
                .toList();
    }

    @Transactional
    public void changeJoinApplyStatus(Long targetGroupId, Long targetUserId, Long leaderId,
            ApprovalStatus approvalStatus) {
        Group targetGroup = groupRepository.findById(targetGroupId)
                .orElseThrow(GroupNotFoundException::new);

        GroupJoinApplyStatusChangeValidationRequest validationRequest =
                GroupJoinApplyStatusChangeValidationRequest.of(leaderId, targetUserId, targetGroup);
        validationFacade.validateGroupJoinApplyStatusChange(validationRequest);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(targetUserId,
                targetGroupId).orElseThrow(GroupUserNotFoundException::new);

        userGroup.approvalJoinStatus(approvalStatus);
        userGroupRepository.save(userGroup);
    }

    @Transactional
    public ChangeGroupTitleAliasResponse changeGroupTitleAlias(Long userId, Long groupId,
            ChangeGroupTitleAliasRequest request) {
        UserIdAndGroupIdValidationRequest validationRequest =
                UserIdAndGroupIdValidationRequest.of(userId, groupId);
        validationFacade.validateGroupTitleAliasChange(validationRequest);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(GroupUserNotFoundException::new);

        userGroup.changeGroupTitleAlias(request.newGroupTitleAlias());

        return ChangeGroupTitleAliasResponse.of(userGroup);
    }

}
