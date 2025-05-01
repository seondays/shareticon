package seondays.shareticon.login.token;

import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import seondays.shareticon.login.UserRole;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtDecoder jwtDecoder;
    private final TokenFactory tokenFactory;
    private final Clock clock;

    public String reissueAccessToken(Cookie[] cookie) {
        String refreshToken = getRefreshToken(cookie);
        validateRefreshToken(refreshToken);

        Jwt jwt = getJwt(refreshToken);
        Map<String, Object> claims = jwt.getClaims();

        UserRole role = UserRole.getUserRoleBy((String) claims.get("role"));
        Long userId = Long.parseLong(jwt.getSubject());

        return tokenFactory.createAccessToken(userId, role, Duration.ofDays(3));
    }

    public void deleteAllRefreshToken(Long userId) {
        tokenRepository.deleteAllByUserId(userId);
    }

    private void validateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = getJwt(refreshToken);

            checkTokenType(jwt);

            checkExpiration(jwt);

            checkRefreshTokenSaved(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("잘못된 리프레시 토큰입니다.");
        }
    }

    private Jwt getJwt(String refreshToken) {
        return jwtDecoder.decode(refreshToken);
    }

    private String getRefreshToken(Cookie[] cookies) {
        String refresh = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }
        return refresh;
    }

    private void checkTokenType(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        TokenType tokenType = TokenType.of((String) claims.get("tokenType"));
        if (!TokenType.isRefreshToken(tokenType)) {
            throw new BadCredentialsException("리프레시 토큰 타입이 아닙니다.");
        }
    }

    private void checkExpiration(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt.isBefore(clock.instant())) {
            throw new BadCredentialsException("리프레시 토큰이 만료되었습니다.");
        }
    }

    private void checkRefreshTokenSaved(String refreshToken) {
        if(!tokenRepository.existsById(refreshToken)) {
            throw new BadCredentialsException("리프레시 토큰이 만료되었습니다.");
        }
    }
}
