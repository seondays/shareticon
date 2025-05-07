package seondays.shareticon.group;

import static seondays.shareticon.group.JoinStatus.JOINED;
import static seondays.shareticon.group.JoinStatus.PENDING;

import java.security.SecureRandom;
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
import seondays.shareticon.group.dto.GroupListResponse;
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
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public Group createGroup(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int maxRetry = 3;
        int retryCount = 0;

        while (retryCount < maxRetry) {
            String inviteCode = createInviteCode();
            try {
                Group newGroup = Group.builder()
                        .leaderUser(user)
                        .inviteCode(inviteCode)
                        .build();
                groupRepository.save(newGroup);

                UserGroup userGroupInfo = UserGroup.builder()
                        .group(newGroup)
                        .user(user)
                        .joinStatus(JoinStatus.JOINED)
                        .build();
                userGroupRepository.save(userGroupInfo);
                return newGroup;
            } catch (DataIntegrityViolationException e) {
                retryCount++;
                log.warn("{} 유저 그룹 생성 시도 중, 초대코드 중복 발생! 재시도 {}/{}", userId, retryCount, maxRetry);
            }
        }
        throw new GroupCreateException();
    }

    public List<GroupListResponse> getAllGroupList(Long userId) {
        return userGroupRepository.findAllByUserId(userId)
                .stream()
                .map(GroupListResponse::of)
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

    public List<ApplyToJoinResponse> getAllApplyToJoinList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return userGroupRepository.findPendingByLeader(userId, PENDING)
                .stream()
                .map(ApplyToJoinResponse::of)
                .toList();
    }

    @Transactional
    public void acceptJoinApply(Long targetGroupId, Long targetUserId, Long leaderId) {
        Group targetGroup = groupRepository.findById(targetGroupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!leaderId.equals(targetGroup.getLeaderUser().getId())) {
            throw new InvalidAcceptGroupJoinApplyException();
        }

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(targetUserId,
                targetGroupId).orElseThrow(
                GroupUserNotFoundException::new);

        if (JoinStatus.isWaitingAcceptJoinApply(userGroup.getJoinStatus())) {
            throw new InvalidJoinGroupException();
        }
        userGroup.updateJoinStatus(JOINED);
        userGroupRepository.save(userGroup);
    }

    private String createInviteCode() {
        final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

}
