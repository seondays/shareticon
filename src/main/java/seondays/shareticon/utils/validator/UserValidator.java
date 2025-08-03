package seondays.shareticon.utils.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final BasicValidator basicValidator;

    public void validateUserProfile(String userNickname) {
        if (userNickname == null || userNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다");
        }
    }

    public void validateUserExist(Long userId) {
        basicValidator.validateUserExist(userId);
    }
}
