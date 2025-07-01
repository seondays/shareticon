package seondays.shareticon.api.group;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

public class GroupRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 초대 코드를 가진 그룹을 조회한다")
    void findByInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        //when
        Optional<Group> result = groupRepository.findByInviteCode(group.getInviteCode());

        //then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getInviteCode()).isEqualTo(inviteCode);

    }

    @Test
    @DisplayName("특정 초대 코드를 가진 그룹이 없는 경우 빈 optional을 조회한다")
    void findByNoExistInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        //when
        String testInviteCode = "test";
        Optional<Group> result = groupRepository.findByInviteCode(testInviteCode);

        //then
        assertThat(result).isEmpty();

    }

    @Test
    @DisplayName("특정 초대 코드를 가진 그룹이 존재하는 경우 true가 반환된다")
    void existByInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        //when
        boolean result = groupRepository.existsByInviteCode(inviteCode);

        //then
        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("특정 초대 코드를 가진 그룹이 존재하지 않는 경우 false가 반환된다")
    void noExistByInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        //when
        String testInviteCode = "test";
        boolean result = groupRepository.existsByInviteCode(testInviteCode);

        //then
        assertThat(result).isFalse();

    }

    @Test
    @DisplayName("그룹을 새로 저장할 때, 생성된 날짜와 시간이 함께 저장된다")
    void auditingGroup() {
        // given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);

        // when
        groupRepository.save(group);

        // then
        Optional<Group> result = groupRepository.findByInviteCode(inviteCode);

        assertThat(result).isPresent();
        assertThat(result.get().getCreatedDateTime()).isNotNull();
        assertThat(result.get().getModifiedDateTime()).isNotNull();
        assertThat(result.get().getInviteCode()).isEqualTo(inviteCode);

        LocalDateTime now = LocalDateTime.now();
        assertThat(result.get().getCreatedDateTime()).isBeforeOrEqualTo(now);
        assertThat(result.get().getModifiedDateTime()).isBeforeOrEqualTo(now);

    }

    @Test
    @DisplayName("유저가 리더를 맡고 있는 그룹을 전부 조회하는 경우, 해당 그룹에 가입된 유저들의 정보인 UserGroup이 함께 조회된다.")
    void findAllByLeaderId() {
        //given
        User leader = User.builder().build();
        userRepository.save(leader);

        Group group1 = Group.createNewGroup(leader, "test", "그룹1");
        Group group2 = Group.createNewGroup(leader, "test2", "그룹2");
        groupRepository.saveAll(List.of(group1, group2));

        UserGroup leaderUserGroup1 = UserGroup.createLeaderUserGroup(leader, group1);
        UserGroup leaderUserGroup2 = UserGroup.createLeaderUserGroup(leader, group2);
        userGroupRepository.saveAll(List.of(leaderUserGroup1, leaderUserGroup2));

        //when
        List<Group> result = groupRepository.findAllByLeaderId(leader.getId());

        //then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(group1, group2);

        for (Group g : result) {
            assertThat(g.getUserGroups()).isNotNull();
            for (UserGroup ug : g.getUserGroups()) {
                assertThat(ug.getUser()).isNotNull();
                assertThat(ug.getGroup()).isNotNull();
            }
        }

    }

    @Test
    @DisplayName("유저가 리더를 맡고 있는 그룹이 없다면 빈 리스트를 조회한다")
    void findAllByNoLeader() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        List<Group> result = groupRepository.findAllByLeaderId(user.getId());

        //then
        assertThat(result).isEmpty();

    }

    public Group createTestGroup(String inviteCode) {
        User user = User.builder().build();
        userRepository.save(user);
        return Group.builder().inviteCode(inviteCode).leaderUser(user).build();
    }

}
