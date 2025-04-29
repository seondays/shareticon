package seondays.shareticon.api.login.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.login.UserRole;
import seondays.shareticon.login.token.RefreshToken;
import seondays.shareticon.login.token.StoredRefreshToken;
import seondays.shareticon.login.token.TokenFactory;
import seondays.shareticon.login.token.TokenRepository;
import seondays.shareticon.login.token.TokenService;

@ActiveProfiles("test")
@SpringBootTest
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenFactory tokenFactory;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RedisTemplate<String, StoredRefreshToken> redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.getRequiredConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이 담긴 쿠키로 엑세스 토큰을 발급한다")
    void createAccessTokenWithValidatedRefreshToken() {
        //given
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        Duration duration = Duration.ofDays(1);
        RefreshToken refreshToken = tokenFactory.createRefreshToken(userId, role, duration);
        tokenRepository.save(refreshToken);

        Cookie[] cookies = {new Cookie("refresh", refreshToken.getToken())};

        //when
        String accessToken = tokenService.reissueAccessToken(cookies);

        //then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).startsWith("Bearer ");
    }

    @Test
    @DisplayName("리프레시 토큰 이외의 토큰 타입으로 엑세스 토큰 발급 시 예외가 발생한다")
    void createAccessTokenWithRefreshTokenType() {
        //given
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        Duration duration = Duration.ofDays(1);
        String NotRefreshToken = tokenFactory.createAccessToken(userId, role, duration);

        Cookie[] cookies = {new Cookie("refresh", NotRefreshToken)};

        //when //then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(cookies)).isInstanceOf(
                BadCredentialsException.class);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 액세스 토큰 발급 시 예외가 발생한다")
    void createAccessTokenWithExpiredToken() throws InterruptedException {
        //given
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        Duration duration = Duration.ofSeconds(1);
        RefreshToken refreshToken = tokenFactory.createRefreshToken(userId, role, duration);

        Cookie[] cookies = {new Cookie("refresh", refreshToken.getToken())};

        //when //then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(cookies)).isInstanceOf(
                BadCredentialsException.class);

    }

    @Test
    @DisplayName("DB에 저장되어 있지 않은 리프레시 토큰으로 엑세스 토큰 발급 시 예외가 발생한다")
    void createAccessTokenWithRefreshTokenNotSaved() {
        //given
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;
        Duration duration = Duration.ofDays(1);
        RefreshToken refreshToken = tokenFactory.createRefreshToken(userId, role, duration);

        Cookie[] cookies = {new Cookie("refresh", refreshToken.getToken())};

        //when //then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(cookies)).isInstanceOf(
                BadCredentialsException.class);
    }

}
