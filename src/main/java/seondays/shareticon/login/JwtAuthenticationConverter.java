package seondays.shareticon.login;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import seondays.shareticon.user.dto.UserOAuth2Dto;

@Component
public class JwtAuthenticationConverter {

    public Converter<Jwt, ? extends AbstractAuthenticationToken> convertToAuthentication() {
        return jwt -> {
            String tokenType = jwt.getClaim("tokenType");
            if (tokenType == null || !tokenType.equals("ACCESS")) {
                throw new IllegalArgumentException();
            }

            UserOAuth2Dto userOAuth2Dto = createUserOAuth2DtoFromJwt(jwt);
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userOAuth2Dto);

            return new UsernamePasswordAuthenticationToken(
                    customOAuth2User, jwt, customOAuth2User.getAuthorities());
        };
    }

    private UserOAuth2Dto createUserOAuth2DtoFromJwt(Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        String role = jwt.getClaim("role");
        String name = jwt.getClaim("name");

        return UserOAuth2Dto.create(userId, name, role);
    }
}
