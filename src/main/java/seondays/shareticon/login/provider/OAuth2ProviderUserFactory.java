package seondays.shareticon.login.provider;

import java.util.Map;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.IllegalOAuthProviderException;
import seondays.shareticon.login.OAuth2Type;

@Component
public class OAuth2ProviderUserFactory {

    public OAuth2Provider getOAuth2User(String registrationId, Map<String, Object> attributes) {
        OAuth2Type providerType = OAuth2Type.getOAuth2TypeBy(registrationId);

        switch (providerType) {
            case KAKAO:
                return new KakaoUser(attributes);

            default:
                throw new IllegalOAuthProviderException();
        }
    }
}
