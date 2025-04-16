package seondays.shareticon.login;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuth2Type {

    KAKAO("kakao");

    private final String registrationId;

    public static OAuth2Type getOAuth2TypeBy(String registrationId) {
        return Arrays.stream(OAuth2Type.values())
                .filter(oAuth2Type -> oAuth2Type.registrationId.equals(registrationId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        registrationId + "에 대응하는 OAuth2Type이 없습니다"));
    }
}
