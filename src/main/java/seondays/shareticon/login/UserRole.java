package seondays.shareticon.login;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER;

    // todo : 커스텀을 예외로 할지 고민
    public static UserRole getUserRoleBy(String role) {
        return Arrays.stream(UserRole.values())
                .filter(u -> u.name().equals(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        role + "에 대응하는 Role이 없습니다"));
    }
}
