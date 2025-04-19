package seondays.shareticon.login.provider;

import java.util.Map;
import seondays.shareticon.login.OAuth2Type;
import seondays.shareticon.login.UserRole;
import seondays.shareticon.user.User;

public class KakaoUser implements OAuth2Provider {

    private Map<String, Object> attributes;

    public KakaoUser(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getNickName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("nickname").toString();
    }

    @Override
    public UserRole getRole() {
        return UserRole.ROLE_USER;
    }

    @Override
    public User toEntity() {
        return User.builder()
                .nickname(getNickName())
                .oauth2Id(getProviderId())
                .oauth2Type(OAuth2Type.KAKAO)
                .role(getRole())
                .build();
    }
}
