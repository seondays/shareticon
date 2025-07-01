package seondays.shareticon.user.dto;

import lombok.Builder;
import seondays.shareticon.user.User;

@Builder
public record UserProfileResponse(Long userId,
                                  String nickName,
                                  String email,
                                  Long joinGroupCount,
                                  Long ownedVoucherCount) {

    public static UserProfileResponse of(User user, Long joinGroupCount, Long ownedVoucherCount) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .nickName(user.getNickname())
                .email(user.getEmail())
                .joinGroupCount(joinGroupCount)
                .ownedVoucherCount(ownedVoucherCount).build();
    }

}
