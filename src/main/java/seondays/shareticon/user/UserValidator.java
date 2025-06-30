package seondays.shareticon.user;

import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateUserProfile(String userNickname) {
        if (userNickname == null || userNickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다");
        }
    }
}
