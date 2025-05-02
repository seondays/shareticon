package seondays.shareticon.api.login.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.api.config.RedisTestContainer;
import seondays.shareticon.login.token.RefreshToken;
import seondays.shareticon.login.token.StoredRefreshToken;
import seondays.shareticon.login.token.TokenRepository;

@ActiveProfiles("test")
@SpringBootTest
public class TokenRepositoryTest extends RedisTestContainer {

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
    @DisplayName("Refresh 토큰을 저장한다")
    void saveRefreshToken() {
        //given
        Long id = 1L;
        String token = "tokenvalue";
        Long ttl = 100L;
        Date expiration = new Date();

        RefreshToken refreshToken = RefreshToken.create(id, token, expiration,ttl);

        //when
        tokenRepository.save(refreshToken);

        //then
        assertThat(tokenRepository.existsById(token)).isTrue();
    }

    @Test
    @DisplayName("사용자 id에 매핑되는 Refresh 토큰을 모두 삭제한다")
    void deleteAllByUserId() {
        //given
        Long userId = 1L;
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";
        Date expiration = new Date();


        RefreshToken refreshToken1 = RefreshToken.create(userId, token1, expiration, 100L);
        RefreshToken refreshToken2 = RefreshToken.create(userId, token2, expiration, 100L);
        RefreshToken refreshToken3 = RefreshToken.create(userId, token3, expiration,100L);
        tokenRepository.save(refreshToken1);
        tokenRepository.save(refreshToken2);
        tokenRepository.save(refreshToken3);

        //when
        tokenRepository.deleteAllByUserId(1L);

        //then
        assertThat(tokenRepository.existsById(token1)).isFalse();
        assertThat(tokenRepository.existsById(token2)).isFalse();
        assertThat(tokenRepository.existsById(token3)).isFalse();

    }

}
