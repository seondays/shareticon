package seondays.shareticon.login.token;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import seondays.shareticon.login.UserRole;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenRepository tokenRepository;

    public String createAccessToken(Long userId, UserRole role, Duration expiresIn) {
        String jwt = createJwt(userId, role, TokenType.ACCESS, expiresIn);
        return addBearerPrefix(jwt);
    }

    public RefreshToken createRefreshToken(Long userId, UserRole role, Duration expiresIn) {
        String jwt = createJwt(userId, role, TokenType.REFRESH, expiresIn);
        return RefreshToken.create(userId, jwt, expiresIn.getSeconds());
    }

    public String reissueAccessToken(Cookie[] cookie) {
        String refreshToken = getRefreshToken(cookie);

        Map<String, Object> claims = validateRefreshToken(refreshToken);

        UserRole role = UserRole.getUserRoleBy((String) claims.get("role"));
        Long userId = (Long) claims.get("userId");

        return createAccessToken(userId, role, Duration.ofDays(3));
    }

    // todo : 검증 매커니즘 고민
    private Map<String, Object> validateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);

            checkTokenType(jwt);

            checkExpiration(jwt);

            checkRefreshTokenSaved(refreshToken);

            return jwt.getClaims();
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.", e);
        }
    }

    private String createJwt(Long userId, UserRole role, TokenType tokenType, Duration expiresIn) {
        Instant now = Instant.now();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn))
                .claim("userId", userId)
                .claim("role", role)
                .claim("tokenType", tokenType)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String getRefreshToken(Cookie[] cookies) {
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

    // todo 예외 추가
    private void checkTokenType(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        TokenType tokenType = TokenType.of((String) claims.get("tokenType"));
        if (!TokenType.isRefreshToken(tokenType)) {
            throw new IllegalArgumentException("리프레시 토큰 타입이 아닙니다.");
        }
    }

    private void checkExpiration(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
        }
    }

    private void checkRefreshTokenSaved(String refreshToken) {
        if(!tokenRepository.existsById(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다.");
        }
    }

    private String addBearerPrefix(String token) {
        return "Bearer " + token;
    }

    private String detachBearerPrefix(String token) {
        return token.split(" ")[1];
    }
}
