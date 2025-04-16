package seondays.shareticon.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;

public class CustomOAuth2User implements OAuth2User {
    private final UserOAuth2Dto userOAuth2Dto;

    public CustomOAuth2User(UserOAuth2Dto userOAuth2Dto) {
        this.userOAuth2Dto = userOAuth2Dto;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userOAuth2Dto.role();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return userOAuth2Dto.name();
    }

    public Long getId() {
        return userOAuth2Dto.userId();
    }

}
