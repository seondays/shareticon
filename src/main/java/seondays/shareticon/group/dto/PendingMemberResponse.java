package seondays.shareticon.group.dto;

import lombok.Builder;
import seondays.shareticon.user.User;

@Builder
public record PendingMemberResponse(Long applyUserId,
                                    String applyUserNickname) {

    public static PendingMemberResponse of(Long applyUserId, String applyUserNickname) {
        return new PendingMemberResponse(applyUserId, applyUserNickname);
    }

    public static PendingMemberResponse from(User user) {
        return new PendingMemberResponse(user.getId(), user.getNickname());
    }
}
