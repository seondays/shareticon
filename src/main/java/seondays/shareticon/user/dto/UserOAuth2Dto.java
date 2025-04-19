package seondays.shareticon.user.dto;

import seondays.shareticon.user.User;

public record UserOAuth2Dto(Long userId,
                            String name,
                            String role
                            ) {

    public static UserOAuth2Dto of(User user) {
        return new UserOAuth2Dto(user.getId(), user.getNickname(), user.getRole().name());
    }

    public static UserOAuth2Dto create(Long userId, String name, String role) {
        return new UserOAuth2Dto(userId, name, role);
    }

}
