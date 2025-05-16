package seondays.shareticon.api.group;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;

@ActiveProfiles("test")
@DataJpaTest
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;
    
    @Test
    @DisplayName("특정 초대 코드를 가진 그룹을 조회한다")
    void findByInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = Group.builder().inviteCode(inviteCode).build();
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
        Group group = Group.builder().inviteCode(inviteCode).build();
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
        Group group = Group.builder().inviteCode(inviteCode).build();
        groupRepository.save(group);

        //when
        boolean result = groupRepository.existsByInviteCode(inviteCode);

        //then
        assertThat(result).isTrue();

    }

    @Test
    @DisplayName("특정 초대 코드를 가진 그룹이 존재하지 않는 경오 false가 반환된다")
    void noExistByInviteCode() {
        //given
        String inviteCode = "ABC";
        Group group = Group.builder().inviteCode(inviteCode).build();
        groupRepository.save(group);

        //when
        String testInviteCode = "test";
        boolean result = groupRepository.existsByInviteCode(testInviteCode);

        //then
        assertThat(result).isFalse();

    }
}
