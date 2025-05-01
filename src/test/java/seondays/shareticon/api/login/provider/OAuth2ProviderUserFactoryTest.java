package seondays.shareticon.api.login.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seondays.shareticon.exception.IllegalOAuthProviderException;
import seondays.shareticon.login.provider.KakaoUser;
import seondays.shareticon.login.provider.OAuth2Provider;
import seondays.shareticon.login.provider.OAuth2ProviderUserFactory;

public class OAuth2ProviderUserFactoryTest {

    private OAuth2ProviderUserFactory oAuth2ProviderUserFactory;

    @BeforeEach
    void setUp() {
        oAuth2ProviderUserFactory = new OAuth2ProviderUserFactory();
    }

    @Test
    @DisplayName("registrationId가 kakao면 kakao 유저가 생성된다")
    void createUserWithKakao() {
        //given
        String registrationId = "kakao";
        Map<String, Object> attributes = new HashMap<>();

        //when
        OAuth2Provider user = oAuth2ProviderUserFactory.getOAuth2User(registrationId,
                attributes);

        //then
        assertThat(user).isInstanceOf(KakaoUser.class);
    }

    @Test
    @DisplayName("해당되는 provider가 없으면 예외가 발생한다")
    void createUserWithNoProvider() {
        //given
        String registrationId = "unknown";
        Map<String, Object> attributes = new HashMap<>();

        //when  //then
        assertThatThrownBy(() ->
                oAuth2ProviderUserFactory.getOAuth2User(registrationId, attributes)).isInstanceOf(
                IllegalOAuthProviderException.class);
    }
}
