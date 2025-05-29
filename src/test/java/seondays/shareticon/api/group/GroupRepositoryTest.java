package seondays.shareticon.api.group;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;

public class GroupRepositoryTest extends RepositoryTestSupport {

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
    @DisplayName("특정 초대 코드를 가진 그룹이 존재하지 않는 경우 false가 반환된다")
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

    @Test
    @DisplayName("그룹을 새로 저장할 때, 생성된 날짜와 시간이 함께 저장된다")
    void auditingGroup() {
        // given
        String inviteCode = "ABC";
        Group group = Group.builder().inviteCode(inviteCode).build();

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

}
