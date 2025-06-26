package seondays.shareticon.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.user.UserService;
import seondays.shareticon.user.dto.UserProfileResponse;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;

public class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @AfterEach
    void tearDown() {
        userGroupRepository.deleteAllInBatch();
        voucherRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저의 프로필 정보를 조회한다")
    void getUserProfile() {
        //given
        String nickname = "닉네임";
        String email = "test@test";
        User user = User.builder().nickname(nickname).email(email).build();
        userRepository.save(user);

        Group group = Group.builder().build();
        Group group2 = Group.builder().build();
        Group group3 = Group.builder().build();
        groupRepository.saveAll(List.of(group3, group2, group));

        linkUserWithGroup(user, group);
        linkUserWithGroup(user, group2);
        linkUserWithGroup(user, group3);

        Voucher voucher = Voucher.builder().user(user).group(group).build();
        Voucher voucher2 = Voucher.builder().user(user).group(group).build();
        voucherRepository.saveAll(List.of(voucher, voucher2));

        //when
        UserProfileResponse response = userService.getUserProfile(user.getId());

        //then
        assertThat(response.nickName()).isEqualTo(nickname);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.joinGroupCount()).isEqualTo(3);
        assertThat(response.ownedVoucherCount()).isEqualTo(2);

    }

    @Test
    @DisplayName("유저 프로필 조회 시 해당하는 유저가 존재하지 않으면 예외가 발생한다")
    void getUserProfileWithUserNoExist() {
        //given
        String nickname = "닉네임";
        String email = "test@test";
        User user = User.builder().id(1L).nickname(nickname).email(email).build();

        //when //then
        assertThatThrownBy(() -> userService.getUserProfile(user.getId())).isInstanceOf(
                UserNotFoundException.class);

    }

    private UserGroup linkUserWithGroup(User user, Group group) {
        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userGroupRepository.save(userGroup);
        return userGroup;
    }
}
