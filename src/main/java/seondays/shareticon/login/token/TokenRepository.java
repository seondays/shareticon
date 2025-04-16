package seondays.shareticon.login.token;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final RedisTemplate<String, StoredRefreshToken> redisTemplate;

    /**
     * Refresh 토큰 저장
     *
     * @param refreshToken
     */
    public void save(RefreshToken refreshToken) {
        String key = makeKey(refreshToken.getToken());
        redisTemplate.opsForValue()
                .set(key, StoredRefreshToken.of(refreshToken),
                        refreshToken.getTimeToLive(), TimeUnit.SECONDS);
    }

    /**
     * Refresh 토큰을 조회합니다.
     *
     * @param token
     * @return
     */
    public Optional<StoredRefreshToken> findByToken(String token) {
        String key = makeKey(token);
        StoredRefreshToken refreshToken = (StoredRefreshToken) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(refreshToken);
    }

    /**
     * Refresh 토큰을 삭제합니다.
     *
     * @param token
     */
    public void deleteById(String token) {
        String key = makeKey(token);
        redisTemplate.delete(key);
    }

    /**
     * Refresh 토큰이 존재하는지 확인합니다.
     *
     * @param token
     * @return
     */
    public boolean existsById(String token) {
        String key = makeKey(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 토큰 값과 키스페이스 값을 합쳐 key를 생성합니다.
     *
     * @param token
     * @return
     */
    private String makeKey(String token) {
        return "refresh:" + token;
    }
}
