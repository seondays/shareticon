package seondays.shareticon.api.userGroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

public class UserGroupRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("유저가 특정 그룹에 가입되어 있는지 조회한다")
    void getUserWithGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userGroupRepository.save(userGroup);

        //when
        boolean result = userGroupRepository.existsByUserIdAndGroupId(user.getId(), group.getId());

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유저가 가입되어 있는 그룹의 정보를 모두 조회한다")
    void getUserWithAllGroup() {
        //given
        User user = User.builder().build();
        User user2 = User.builder().build();
        userRepository.saveAll(List.of(user, user2));

        String inviteCode1 = "ABC";
        Group group1 = createTestGroup(inviteCode1);
        String inviteCode2 = "DEF";
        Group group2 = createTestGroup(inviteCode2);
        groupRepository.saveAll(List.of(group1, group2));

        UserGroup userGroup1 = UserGroup.builder().user(user).joinStatus(JoinStatus.JOINED).group(group1).build();
        UserGroup userGroup2 = UserGroup.builder().user(user).joinStatus(JoinStatus.JOINED).group(group2).build();
        UserGroup userGroup3 = UserGroup.builder().user(user2).joinStatus(JoinStatus.REJECTED).group(group1).build();
        UserGroup userGroup4 = UserGroup.builder().user(user2).joinStatus(JoinStatus.JOINED).group(group2).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2, userGroup3, userGroup4));

        //when
        List<GroupListResponse> result = userGroupRepository
                .findGroupsWithMemberCountByUserIdAndStatus(
                        user.getId(), JoinStatus.getStatusesForAcceptedGroupMembers());

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("groupId", "groupTitleAlias", "memberCount")
                .containsExactlyInAnyOrder(
                        tuple(group1.getId(), group1.getTitle(), 1L),
                        tuple(group2.getId(), group2.getTitle(), 2L)
                );

    }

    @Test
    @DisplayName("가입된 그룹이 없는 유저의 그룹 정보를 모두 조회하는 경우 빈 리스트를 반환한다")
    void getUserWithAllGroupNotExist() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        List<GroupListResponse> result = userGroupRepository
                .findGroupsWithMemberCountByUserIdAndStatus(
                user.getId(), JoinStatus.getStatusesForAcceptedGroupMembers());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유저와 그룹 id를 가지고 유저 그룹 정보를 조회한다")
    void getUserGroupWithUserIdAndGroupId() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().group(group).user(user).build();
        userGroupRepository.save(userGroup);

        //when
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), group.getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(user);
        assertThat(result.get().getGroup()).isEqualTo(group);

    }

    @Test
    @DisplayName("유저와 그룹 id에 해당하는 유저 그룹 정보가 없다면 빈 optional을 조회한다")
    void getUserGroupWithNotExistUserIdAndGroupId() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        //when
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), group.getId());

        //then
        assertThat(result).isEmpty();

    }

    @Test
    @DisplayName("그룹 리더의 Id와 가입 대기 상태를 가지고 유저 그룹 정보를 조회한다")
    void getUserGroupWithLeaderIdAndJoinStatus() {
        //given
        User leader = User.builder().build();
        User user = User.builder().build();
        userRepository.saveAll(List.of(leader, user));

        Group group = Group.builder().leaderUser(leader).build();
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().user(user).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.save(userGroup);

        //when
        List<UserGroup> result = userGroupRepository.findByLeaderAndJoinStatus(
                leader.getId(), JoinStatus.PENDING);

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("id")
                .contains(userGroup.getId());

    }

    @Test
    @DisplayName("유저 그룹 정보를 저장할 때, 생성된 날짜와 시간이 함께 저장된다")
    void auditingUserGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userGroupRepository.save(userGroup);

        //when
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), group.getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getCreatedDateTime()).isNotNull();
        assertThat(result.get().getModifiedDateTime()).isNotNull();

        LocalDateTime now = LocalDateTime.now();
        assertThat(result.get().getCreatedDateTime()).isBeforeOrEqualTo(now);
        assertThat(result.get().getModifiedDateTime()).isBeforeOrEqualTo(now);

    }

    @Test
    @DisplayName("특정 유저가 가입되어 있는 전체 그룹의 수를 조회한다")
    void getUserJoinGroupCount() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);

        String inviteCode2 = "DEF";
        Group group2 = createTestGroup(inviteCode2);
        groupRepository.saveAll(List.of(group, group2));

        UserGroup userGroup = UserGroup.builder().joinStatus(JoinStatus.JOINED).user(user).group(group).build();
        UserGroup userGroup2 = UserGroup.builder().joinStatus(JoinStatus.JOINED).user(user).group(group2).build();
        userGroupRepository.saveAll(List.of(userGroup, userGroup2));

        //when
        Long result = userGroupRepository.countByUserIdAndJoined(user.getId());

        //then
        assertThat(result).isEqualTo(2);

    }

    @Test
    @DisplayName("그룹 가입 상태가 JOINED가 아닌 경우에는 전체 그룹 수에 포함되지 않는다")
    void getUserJoinGroupCountWithJoined() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);

        String inviteCode2 = "DEF";
        Group group2 = createTestGroup(inviteCode2);

        String inviteCode3 = "GHI";
        Group group3 = createTestGroup(inviteCode3);

        String inviteCode4 = "JKL";
        Group group4 = createTestGroup(inviteCode4);
        groupRepository.saveAll(List.of(group, group2, group3, group4));

        UserGroup userGroup = UserGroup.builder().joinStatus(JoinStatus.JOINED).user(user).group(group).build();
        UserGroup userGroup2 = UserGroup.builder().joinStatus(JoinStatus.WITHDRAWN).user(user).group(group2).build();
        UserGroup userGroup3 = UserGroup.builder().joinStatus(JoinStatus.REJECTED).user(user).group(group2).build();
        UserGroup userGroup4 = UserGroup.builder().joinStatus(JoinStatus.PENDING).user(user).group(group2).build();
        userGroupRepository.saveAll(List.of(userGroup, userGroup2, userGroup3, userGroup4));

        //when
        Long result = userGroupRepository.countByUserIdAndJoined(user.getId());

        //then
        assertThat(result).isEqualTo(1);

    }

    @Test
    @DisplayName("특정 유저가 가입되어 있는 전체 그룹의 수가 0개여도 정상적으로 조회된다")
    void getUserJoinGroupCountZero() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        Long result = userGroupRepository.countByUserIdAndJoined(user.getId());

        //then
        assertThat(result).isZero();

    }

    public Group createTestGroup(String inviteCode) {
        User user = User.builder().build();
        userRepository.save(user);
        return Group.builder().inviteCode(inviteCode).leaderUser(user).build();
    }
}
