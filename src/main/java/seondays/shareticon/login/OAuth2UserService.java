package seondays.shareticon.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.login.provider.OAuth2Provider;
import seondays.shareticon.login.provider.OAuth2ProviderUserFactory;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.user.dto.UserOAuth2Dto;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2ProviderUserFactory oAuth2ProviderUserFactory;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // todo : logging
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Provider oAuth2ProviderUser =
                oAuth2ProviderUserFactory.getOAuth2User(registrationId, oAuth2User.getAttributes());

        User user = userRepository.findByOauth2IdAndOauth2Type(oAuth2ProviderUser.getProviderId(),
                OAuth2Type.getOAuth2TypeBy(registrationId))
                .orElseGet(oAuth2ProviderUser::toEntity);

        userRepository.save(user);

        return new CustomOAuth2User(UserOAuth2Dto.of(user));
    }
}
