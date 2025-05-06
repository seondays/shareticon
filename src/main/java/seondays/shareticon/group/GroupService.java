package seondays.shareticon.group;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.GroupCreateException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;

@Slf4j
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

    private String createInviteCode() {
        final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

}
