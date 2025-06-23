package seondays.shareticon.group;

import static seondays.shareticon.group.JoinStatus.JOINED;
import static seondays.shareticon.group.JoinStatus.PENDING;
import static seondays.shareticon.group.JoinStatus.REJECTED;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.AlreadyAppliedToGroupException;
import seondays.shareticon.exception.GroupCreateException;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.GroupUserNotFoundException;
import seondays.shareticon.exception.InvalidAcceptGroupJoinApplyException;
import seondays.shareticon.exception.InvalidJoinGroupException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.dto.ApplyToJoinRequest;
import seondays.shareticon.group.dto.ApplyToJoinResponse;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasRequest;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasResponse;
import seondays.shareticon.group.dto.CreateGroupRequest;
import seondays.shareticon.group.dto.GroupJoinApplyStatusChangeValidationRequest;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.group.dto.GroupResponse;
import seondays.shareticon.group.dto.GroupTitleAliasChangeValidationRequest;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final RandomCodeFactory randomCodeFactory;
    private final GroupValidator groupValidator;

    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User leaderUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int maxRetry = 3;
        int retryCount = 0;

        while (retryCount < maxRetry) {

            try {
                String inviteCode = randomCodeFactory.createInviteCode();

                Group newGroup = Group.createNewGroup(leaderUser, inviteCode, request.title());
                groupRepository.save(newGroup);

                UserGroup leaderUserGroup = UserGroup.createLeaderUserGroup(leaderUser, newGroup);
                userGroupRepository.save(leaderUserGroup);

                return GroupResponse.of(newGroup);
            } catch (DataIntegrityViolationException e) {
                retryCount++;
                log.warn("{} 유저 그룹 생성 시도 중, 초대코드 중복 발생 : 재시도 {}/{}", userId,
                        retryCount, maxRetry);
            }
        }
        throw new GroupCreateException();
    }

    public List<GroupListResponse> getAllGroupList(Long userId) {
        return userGroupRepository.findGroupsWithMemberCountByUserId(userId)
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

    public List<ApplyToJoinResponse> getAllApplyToJoinList(Long leaderUserId) {
        User leaderUser = userRepository.findById(leaderUserId)
                .orElseThrow(UserNotFoundException::new);

        return userGroupRepository.findByLeaderAndJoinStatus(leaderUser.getId(), PENDING)
                .stream()
                .map(ApplyToJoinResponse::of)
                .toList();
    }

    @Transactional
    public void changeJoinApplyStatus(Long targetGroupId, Long targetUserId, Long leaderId,
            ApprovalStatus approvalStatus) {
        Group targetGroup = groupRepository.findById(targetGroupId)
                .orElseThrow(GroupNotFoundException::new);

        GroupJoinApplyStatusChangeValidationRequest validationRequest =
                GroupJoinApplyStatusChangeValidationRequest.builder()
                        .leaderId(leaderId)
                        .targetUserId(targetUserId)
                        .targetGroup(targetGroup).build();
        groupValidator.validateGroupJoinApplyStatusChange(validationRequest);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(targetUserId,
                targetGroupId).orElseThrow(GroupUserNotFoundException::new);

        userGroup.approvalJoinStatus(approvalStatus);
        userGroupRepository.save(userGroup);
    }

    @Transactional
    public ChangeGroupTitleAliasResponse changeGroupTitleAlias(Long userId, Long groupId,
            ChangeGroupTitleAliasRequest request) {
        GroupTitleAliasChangeValidationRequest validationRequest =
                GroupTitleAliasChangeValidationRequest.builder()
                        .requestUserId(userId)
                        .targetGroupId(groupId).build();
        groupValidator.validateGroupTitleAliasChange(validationRequest);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(GroupUserNotFoundException::new);

        userGroup.changeGroupTitleAlias(request.newGroupTitleAlias());

        return ChangeGroupTitleAliasResponse.of(userGroup);
    }

}
