package seondays.shareticon.utils.validator;

import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateUserProfile(String userNickname) {
        if (userNickname == null || userNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다");
        }
    }
}
