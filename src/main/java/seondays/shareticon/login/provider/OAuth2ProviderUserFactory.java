package seondays.shareticon.login.provider;

import java.util.Map;
import org.springframework.stereotype.Component;
import seondays.shareticon.exception.IllegalOAuthProviderException;

@Component
public class OAuth2ProviderUserFactory {

    public OAuth2Provider getOAuth2User(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return new KakaoUser(attributes);
        }

        throw new IllegalOAuthProviderException();
    }
}
