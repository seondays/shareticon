package seondays.shareticon.api.userGroup;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

@ActiveProfiles("test")
@DataJpaTest
public class UserGroupRepositoryTest {

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
}
