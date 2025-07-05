package seondays.shareticon.group;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.GroupCreateException;
import seondays.shareticon.group.dto.CreateGroupRequest;
import seondays.shareticon.group.dto.GroupResponse;
import seondays.shareticon.user.User;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

@Component
@RequiredArgsConstructor
public class GroupFactory {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final RandomCodeFactory randomCodeFactory;

    @Retryable(
            retryFor = {DataIntegrityViolationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    @Transactional
    public GroupResponse createGroupWithRetry(User leaderUser, CreateGroupRequest request) {
        String inviteCode = randomCodeFactory.createInviteCode();

        Group newGroup = Group.createNewGroup(leaderUser, inviteCode, request.title());
        groupRepository.save(newGroup);

        UserGroup leaderUserGroup = UserGroup.createLeaderUserGroup(leaderUser, newGroup);
        userGroupRepository.save(leaderUserGroup);

        return GroupResponse.of(newGroup);
    }

    @Recover
    public GroupResponse recoverGroupCreation(DataIntegrityViolationException e, User leaderUser,
            CreateGroupRequest request) {
        throw new GroupCreateException();
    }
}
