package seondays.shareticon.login.provider;

import seondays.shareticon.login.UserRole;
import seondays.shareticon.user.User;

public interface OAuth2Provider {

    String getProviderId();
    String getNickName();
    UserRole getRole();
    User toEntity();
}
