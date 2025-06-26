package seondays.shareticon.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.user.dto.UserProfileResponse;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.VoucherRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final VoucherRepository voucherRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        Long joinGroupCount = userGroupRepository.countByUserId(userId);

        Long ownedVoucherCount = voucherRepository.countByUserId(userId);

        return UserProfileResponse.of(user, joinGroupCount, ownedVoucherCount);
    }
}
