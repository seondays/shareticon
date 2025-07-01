package seondays.shareticon.login.provider;

import seondays.shareticon.login.UserRole;
import seondays.shareticon.user.User;

public interface OAuth2Provider {

    String getProviderId();
    String getNickName();
    String getEmail();
    UserRole getRole();
    User toEntity();
}
