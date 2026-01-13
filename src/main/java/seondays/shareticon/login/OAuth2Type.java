package seondays.shareticon.login;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import seondays.shareticon.exception.business.IllegalOAuthProviderException;

@Getter
@RequiredArgsConstructor
public enum OAuth2Type {

    KAKAO("kakao");

    private final String registrationId;

    public static OAuth2Type getOAuth2TypeBy(String registrationId) {
        return Arrays.stream(OAuth2Type.values())
                .filter(t -> t.registrationId.equalsIgnoreCase(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalOAuthProviderException(registrationId));
    }
}
