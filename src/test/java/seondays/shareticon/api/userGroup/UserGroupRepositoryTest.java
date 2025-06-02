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
        Group group = Group.builder().build();
        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userRepository.save(user);
        groupRepository.save(group);
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

        Group group1 = Group.builder().build();
        Group group2 = Group.builder().build();

        UserGroup userGroup1 = UserGroup.builder().user(user).group(group1).build();
        UserGroup userGroup2 = UserGroup.builder().user(user).group(group2).build();
        UserGroup userGroup3 = UserGroup.builder().user(user2).group(group2).build();

        userRepository.saveAll(List.of(user, user2));
        groupRepository.saveAll(List.of(group1, group2));
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2, userGroup3));

        //when
        List<GroupListResponse> result = userGroupRepository.findGroupsWithMemberCountByUserId(user.getId());

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("groupId", "groupTitleAlias", "memberCount")
                .containsExactlyInAnyOrder(
                        tuple(group1.getId(), group1.getTitle(), 1),
                        tuple(group2.getId(), group2.getTitle(), 2)
                );

    }

    @Test
    @DisplayName("가입된 그룹이 없는 유저의 그룹 정보를 모두 조회하는 경우 빈 리스트를 반환한다")
    void getUserWithAllGroupNotExist() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        List<GroupListResponse> result = userGroupRepository.findGroupsWithMemberCountByUserId(user.getId());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유저와 그룹 id를 가지고 유저 그룹 정보를 조회한다")
    void getUserGroupWithUserIdAndGroupId() {
        //given
        User user = User.builder().build();
        Group group = Group.builder().build();
        userRepository.save(user);
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
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().build();
        userGroupRepository.save(userGroup);

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

        Group group = Group.builder().leaderUser(leader).build();
        userRepository.saveAll(List.of(leader,user));
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().user(user).group(group).joinStatus(JoinStatus.PENDING).build();
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
        Group group = Group.builder().build();
        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userRepository.save(user);
        groupRepository.save(group);
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
}
