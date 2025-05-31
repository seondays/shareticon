package seondays.shareticon.api.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.AlreadyAppliedToGroupException;
import seondays.shareticon.exception.GroupCreateException;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.InvalidAcceptGroupJoinApplyException;
import seondays.shareticon.exception.InvalidJoinGroupException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.ApprovalStatus;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.GroupService;
import seondays.shareticon.group.JoinStatus;
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

@Transactional
public class GroupServiceTest extends IntegrationTestSupport {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Test
    @DisplayName("그룹을 생성한다")
    void createGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String groupTitle = "그룹이름";
        CreateGroupRequest request = new CreateGroupRequest(groupTitle);

        //when
        GroupResponse groupResponse = groupService.createGroup(user.getId(), request);

        //then
        assertThat(groupResponse).isNotNull();
    }

    @Test
    @DisplayName("그룹을 생성하는 유저는 DB에 저장된 회원이어야 한다")
    void createGroupWithoutExistUser() {
        //given
        User user = User.builder().id(1L).build();

        String groupTitle = "그룹이름";
        CreateGroupRequest request = new CreateGroupRequest(groupTitle);

        //when //then
        assertThatThrownBy(() -> groupService.createGroup(user.getId(), request)).isInstanceOf(
                UserNotFoundException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInviteCodeScenarios")
    @DisplayName("그룹 코드 중복 시 재시도 및 예외 발생 시나리오를 테스트한다")
    void createGroupWithRetry(String displayName, List<String> inviteCodes, String expectedCode,
            boolean expectException) {
        //given
        User user = userRepository.save(User.builder().build());
        groupRepository.save(Group.builder().leaderUser(user).inviteCode("AAAAAAAA").build());

        String groupTitle = "그룹이름";
        CreateGroupRequest request = new CreateGroupRequest(groupTitle);

        doReturn(inviteCodes.get(0), inviteCodes.get(1), inviteCodes.get(2))
                .when(randomCodeFactory).createInviteCode();

        //when //then
        if (expectException) {
            assertThatThrownBy(() -> groupService.createGroup(user.getId(), request))
                    .isInstanceOf(GroupCreateException.class);
        } else {
            GroupResponse groupResponse = groupService.createGroup(user.getId(), request);
            assertThat(groupResponse.inviteCode()).isEqualTo(expectedCode);
        }
    }

    static Stream<Arguments> provideInviteCodeScenarios() {
        return Stream.of(
                Arguments.of("그룹 코드가 중복이 되어 그룹 생성이 실패하는 경우 최대 3회까지 재시도한다",
                        List.of("AAAAAAAA", "AAAAAAAA", "BBBBBBBB"),
                        "BBBBBBBB",
                        false),
                Arguments.of("그룹 코드가 중복되어 그룹 생성이 3회 이상 실패하는 경우에는 예외가 발생한다",
                        List.of("AAAAAAAA", "AAAAAAAA", "AAAAAAAA"),
                        null,
                        true)
        );
    }

    @Test
    @DisplayName("그룹 생성 시, 유저 그룹 테이블에도 그룹 이름 별명 정보가 저장된다.")
    void createGroupAndSaveAliasTable() {
        User user = User.builder().build();
        userRepository.save(user);

        String groupTitle = "그룹이름";
        CreateGroupRequest request = new CreateGroupRequest(groupTitle);

        //when
        GroupResponse createdGroup = groupService.createGroup(user.getId(), request);

        //then
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), createdGroup.id());

        assertThat(result).isPresent();
        assertThat(result.get().getGroupTitleAlias()).isEqualTo(groupTitle);

    }

    @Test
    @DisplayName("해당 유저의 전체 그룹 리스트를 조회한다")
    void getAllGroupList() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String group1Title = "1번그룹";
        String group2Title = "2번그룹";
        Group group1 = Group.builder().title(group1Title).build();
        Group group2 = Group.builder().title(group2Title).build();
        groupRepository.saveAll(List.of(group1, group2));

        UserGroup userGroup1 = UserGroup.builder().user(user).group(group1).groupTitleAlias(group1.getTitle()).build();
        UserGroup userGroup2 = UserGroup.builder().user(user).group(group2).groupTitleAlias(group2.getTitle()).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when
        List<GroupListResponse> responseList = groupService.getAllGroupList(user.getId());

        //then
        assertThat(responseList).hasSize(2);
        assertThat(responseList).extracting("groupId", "groupTitleAlias")
                .contains(tuple(group1.getId(), group1Title), tuple(group2.getId(), group2Title));
    }

    @Test
    @DisplayName("유저가 가입된 그룹이 없다면, 해당 유저의 전체 그룹 리스트를 조회 시 빈 리스트가 조회된다")
    void getAllGroupListWhenNoGroupJoin() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        List<GroupListResponse> responseList = groupService.getAllGroupList(user.getId());

        //then
        assertThat(responseList).isEmpty();
    }

    @Test
    @DisplayName("유효한 그룹 코드를 가지고 가입을 신청한다")
    void applyToJoinGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().inviteCode("ok").build();
        groupRepository.save(group);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when
        groupService.applyToJoinGroup(request, user.getId());

        //then
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), group.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getJoinStatus()).isEqualTo(JoinStatus.PENDING);
        assertThat(result.get().getGroup().getInviteCode()).isEqualTo("ok");
        assertThat(result.get().getGroup().getId()).isEqualTo(group.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("그룹 가입을 신청하는 유저는 DB에 저장된 회원이어야 한다")
    void applyToJoinGroupWithoutExistUser() {
        //given
        User user = User.builder().id(1L).build();

        Group group = Group.builder().inviteCode("ok").build();
        groupRepository.save(group);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when //then
        assertThatThrownBy(() -> groupService.applyToJoinGroup(request, user.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("가입을 신청하는 그룹은 존재하는 그룹이어야 한다")
    void applyToJoinGroupWithoutExistGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when //then
        assertThatThrownBy(() -> groupService.applyToJoinGroup(request, user.getId()))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    @DisplayName("이미 가입 신청한 그룹에 다시 신청할 수는 없다")
    void applyToJoinGroupAlreadyPending() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().inviteCode("ok").build();
        groupRepository.save(group);

        JoinStatus status = JoinStatus.PENDING;
        UserGroup userGroup = UserGroup.builder().user(user).group(group).joinStatus(status)
                .build();
        userGroupRepository.save(userGroup);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when //then
        assertThatThrownBy(() -> groupService.applyToJoinGroup(request, user.getId()))
                .isInstanceOf(AlreadyAppliedToGroupException.class);
    }

    @Test
    @DisplayName("이미 가입된 그룹에 다시 신청할 수는 없다")
    void applyToJoinGroupAlreadyJoined() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().inviteCode("ok").build();
        groupRepository.save(group);

        JoinStatus status = JoinStatus.JOINED;
        UserGroup userGroup = UserGroup.builder().user(user).group(group).joinStatus(status)
                .build();
        userGroupRepository.save(userGroup);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when //then
        assertThatThrownBy(() -> groupService.applyToJoinGroup(request, user.getId()))
                .isInstanceOf(AlreadyAppliedToGroupException.class);
    }

    @Test
    @DisplayName("거절된 상태에서는 다시 가입 신청이 가능하다")
    void applyToJoinGroupStatusRejected() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().inviteCode("ok").build();
        groupRepository.save(group);

        JoinStatus status = JoinStatus.REJECTED;
        UserGroup userGroup = UserGroup.builder().user(user).group(group).joinStatus(status)
                .build();
        userGroupRepository.save(userGroup);

        ApplyToJoinRequest request = new ApplyToJoinRequest("ok");

        //when
        groupService.applyToJoinGroup(request, user.getId());

        //then
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                user.getId(), group.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getJoinStatus()).isEqualTo(JoinStatus.PENDING);

        Group resultGroup = result.get().getGroup();

        assertThat(resultGroup).isNotNull();
        assertThat(resultGroup.getInviteCode()).isEqualTo("ok");
        assertThat(resultGroup.getId()).isEqualTo(group.getId());
    }

    @Test
    @DisplayName("리더에게 들어온 그룹 신청 내역 목록을 조회한다")
    void getAllApplyToJoinList() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(leaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when
        List<ApplyToJoinResponse> result = groupService.getAllApplyToJoinList(
                leaderUser.getId());

        //then
        assertThat(result).hasSize(1)
                .extracting("applyUserId", "targetGroupId")
                .contains(tuple(pendingUser.getId(), group.getId()));
    }

    @Test
    @DisplayName("리더를 맡고 있는 그룹이 없다면, 그룹 신청 내역 목록을 조회 시 빈 리스트가 조회된다")
    void getAllApplyToJoinListWithNoLeaderUser() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(leaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when
        List<ApplyToJoinResponse> result = groupService.getAllApplyToJoinList(
                leaderUser.getId());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("그룹 신청 내역을 조회하는 유저는 DB에 저장된 회원이어야 한다")
    void getAllApplyToJoinListWithoutExistUser() {
        //given
        User user = User.builder().id(1L).build();

        //when //then
        assertThatThrownBy(() -> groupService.getAllApplyToJoinList(user.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("그룹 가입 신청을 승낙한다")
    void changeJoinApplyStatusApproved() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(leaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(), leaderUser.getId(),
                leaderDecision);

        //then
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                pendingUser.getId(), group.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getJoinStatus()).isEqualTo(JoinStatus.JOINED);
        assertThat(result.get().getUser()).isEqualTo(pendingUser);
    }

    @Test
    @DisplayName("그룹 가입 신청을 거절한다")
    void changeJoinApplyStatusRejected() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(leaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when
        ApprovalStatus leaderDecision = ApprovalStatus.REJECTED;
        groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(), leaderUser.getId(),
                leaderDecision);

        //then
        Optional<UserGroup> result = userGroupRepository.findByUserIdAndGroupId(
                pendingUser.getId(), group.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getJoinStatus()).isEqualTo(JoinStatus.REJECTED);
        assertThat(result.get().getUser()).isEqualTo(pendingUser);
    }

    @Test
    @DisplayName("그룹 가입 신청 여부 처리 시, 가입 대기중이 아닌 유저는 처리 대상이 아니다")
    void changeJoinApplyWithoutPendingUser() {
        //given
        User leaderUser = User.builder().build();
        User joinedUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(joinedUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(leaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(joinedUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), joinedUser.getId(),
                        leaderUser.getId(), leaderDecision))
                .isInstanceOf(InvalidJoinGroupException.class);
    }

    @Test
    @DisplayName("그룹 가입 신청 여부 처리 시, 존재하는 그룹에 관련된 요청을 처리해야 한다")
    void changeJoinApplyWithoutExistGroup() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().id(1L).leaderUser(leaderUser).build();

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(),
                        leaderUser.getId(), leaderDecision))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    @DisplayName("그룹 가입 신청 여부를 결정하는 유저는 그룹 리더여야 한다")
    void changeJoinApplyWithoutGroupLeader() {
        //given
        User notLeaderUser = User.builder().build();
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(notLeaderUser);
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup1 = UserGroup.builder().user(notLeaderUser).group(group)
                .joinStatus(JoinStatus.JOINED).build();
        UserGroup userGroup2 = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.saveAll(List.of(userGroup1, userGroup2));

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(),
                        notLeaderUser.getId(), leaderDecision))
                .isInstanceOf(InvalidAcceptGroupJoinApplyException.class);
    }

    @Test
    @DisplayName("그룹 가입 신청 여부를 결정하는 리더 유저는 DB에 저장된 회원이어야 한다")
    void changeJoinApplyWithoutExistLeaderUser() {
        //given
        User leaderUser = User.builder().id(123L).build();
        User pendingUser = User.builder().build();
        userRepository.save(pendingUser);

        Group group = Group.builder().build();
        groupRepository.save(group);

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(),
                        leaderUser.getId(), leaderDecision))
                .isInstanceOf(InvalidAcceptGroupJoinApplyException.class);
    }

    @Test
    @DisplayName("그룹 가입 신청 여부를 결정하는 리더는 해당 그룹에 속해있어야 한다")
    void chaneJoinApplyWithoutJoinedLeader() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().build();
        userRepository.save(leaderUser);
        userRepository.save(pendingUser);

        Group group = Group.builder().leaderUser(leaderUser).build();
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().user(pendingUser).group(group)
                .joinStatus(JoinStatus.PENDING).build();
        userGroupRepository.save(userGroup);

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(),
                        leaderUser.getId(), leaderDecision))
                .isInstanceOf(InvalidAcceptGroupJoinApplyException.class);
    }

    @Test
    @DisplayName("그룹 가입을 신청하는 유저는 DB에 저장된 회원이어야 한다")
    void changeJoinApplyWithoutExistUser() {
        //given
        User leaderUser = User.builder().build();
        User pendingUser = User.builder().id(123L).build();
        userRepository.save(leaderUser);

        Group group = Group.builder().build();
        groupRepository.save(group);

        //when //then
        ApprovalStatus leaderDecision = ApprovalStatus.APPROVED;
        assertThatThrownBy(
                () -> groupService.changeJoinApplyStatus(group.getId(), pendingUser.getId(),
                        leaderUser.getId(), leaderDecision))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동시에 그룹 생성 요청이 들어와도 inviteCode 중복 발생 시 최대 1개만 성공해야 한다")
    void testConcurrentGroupCreationWithDuplicateInviteCode() throws InterruptedException {
        // given
        User leaderUser = User.builder().build();
        userRepository.save(leaderUser);
        doReturn("DUPLICATE").when(randomCodeFactory).createInviteCode();

        String groupTitle = "그룹이름";
        CreateGroupRequest request = new CreateGroupRequest(groupTitle);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    groupService.createGroup(leaderUser.getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        // tearDown
        userGroupRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("그룹명 별칭을 변경한다")
    void changeGroupTitleAlias() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().build();
        groupRepository.save(group);

        UserGroup userGroup = UserGroup.builder().group(group).user(user)
                .groupTitleAlias(group.getTitle()).build();
        userGroupRepository.save(userGroup);

        String newAlias = "새로운별칭";
        ChangeGroupTitleAliasRequest request = new ChangeGroupTitleAliasRequest(newAlias);

        //when
        ChangeGroupTitleAliasResponse result = groupService.changeGroupTitleAlias(
                user.getId(), group.getId(), request);

        //then
        assertThat(result.titleAlias()).isEqualTo(newAlias);

    }
}
