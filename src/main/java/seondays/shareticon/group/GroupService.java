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
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.group.dto.GroupResponse;
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

    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int maxRetry = 3;
        int retryCount = 0;

        while (retryCount < maxRetry) {

            try {
                String inviteCode = randomCodeFactory.createInviteCode();

                Group newGroup = Group.builder()
                        .leaderUser(user)
                        .inviteCode(inviteCode)
                        .title(request.title())
                        .build();
                groupRepository.save(newGroup);

                userGroupRepository.save(UserGroup.builder()
                        .group(newGroup)
                        .user(user)
                        .groupTitleAlias(newGroup.getTitle())
                        .joinStatus(JoinStatus.JOINED)
                        .build());

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

        if (existingUserGroup.isEmpty()) {
            UserGroup userGroupInfo = UserGroup.builder()
                    .group(group)
                    .user(user)
                    .joinStatus(PENDING)
                    .build();
            userGroupRepository.save(userGroupInfo);
            return;
        }

        UserGroup userGroup = existingUserGroup.get();
        JoinStatus status = userGroup.getJoinStatus();

        if (JoinStatus.isAlreadyApplied(status)) {
            throw new AlreadyAppliedToGroupException();
        }

        userGroup.updateJoinStatus(PENDING);
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

        if (!userRepository.existsById(targetUserId)) {
            throw new UserNotFoundException();
        }

        validateLeader(leaderId, targetGroup);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(targetUserId,
                targetGroupId).orElseThrow(GroupUserNotFoundException::new);

        if (!JoinStatus.isWaitingAcceptJoinApply(userGroup.getJoinStatus())) {
            throw new InvalidJoinGroupException();
        }

        if (ApprovalStatus.isApproved(approvalStatus)) {
            userGroup.updateJoinStatus(JOINED);
            userGroupRepository.save(userGroup);
            return;
        }
        userGroup.updateJoinStatus(REJECTED);
        userGroupRepository.save(userGroup);
    }

    @Transactional
    public ChangeGroupTitleAliasResponse changeGroupTitleAlias(Long userId, Long groupId,
            ChangeGroupTitleAliasRequest request) {
        validateUserAndGroupExist(userId, groupId);

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(GroupUserNotFoundException::new);

        userGroup.changeGroupTitleAlias(request.newGroupTitleAlias());

        return ChangeGroupTitleAliasResponse.of(userGroup);
    }

    private void validateLeader(Long leaderId, Group group) {
        if (userRepository.findById(leaderId).isEmpty()) {
            throw new InvalidAcceptGroupJoinApplyException();
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
