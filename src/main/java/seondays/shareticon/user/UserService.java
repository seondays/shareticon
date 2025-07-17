package seondays.shareticon.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.user.dto.UserProfileChangeRequest;
import seondays.shareticon.user.dto.UserProfileResponse;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.VoucherRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final VoucherRepository voucherRepository;
    private final UserValidator userValidator;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Long joinGroupCount = userGroupRepository.countByUserIdAndJoined(userId);

        Long ownedVoucherCount = voucherRepository.countByUserIdAndIsDeletedFalse(userId);

        return UserProfileResponse.of(user, joinGroupCount, ownedVoucherCount);
    }

    @Transactional
    public void changeUserProfile(Long userId, UserProfileChangeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String newNickname = request.newNickname();

        userValidator.validateUserProfile(newNickname);

        user.changeNickname(newNickname);
    }
}
