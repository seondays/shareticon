package seondays.shareticon.login.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import seondays.shareticon.login.UserRole;

@Component
@RequiredArgsConstructor
public class TokenFactory {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public String createAccessToken(Long userId, UserRole role, Duration expiresIn) {
        String jwt = createJwt(userId, role, TokenType.ACCESS, expiresIn);
        return addBearerPrefix(jwt);
    }

    public RefreshToken createRefreshToken(Long userId, UserRole role, Duration expiresIn) {
        String jwt = createJwt(userId, role, TokenType.REFRESH, expiresIn);
        return RefreshToken.create(userId, jwt, expiresIn.getSeconds());
    }

    private String createJwt(Long userId, UserRole role, TokenType tokenType, Duration expiresIn) {
        Instant now = clock.instant();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn))
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", tokenType)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String addBearerPrefix(String token) {
        return "Bearer " + token;
    }
}
