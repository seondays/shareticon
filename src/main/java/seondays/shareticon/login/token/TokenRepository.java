package seondays.shareticon.login.token;

import java.util.Set;
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
     * 사용자 id에 매핑되는 Refresh 토큰을 모두 삭제합니다.
     *
     * @param userId
     */
    public void deleteAllByUserId(Long userId) {
        String userKey = makeUserIdKey(userId);

        redisTemplate.opsForSet().members(userKey)
                .stream().map(t -> makeTokenKey(t.value()))
                .forEach(redisTemplate::delete);

        redisTemplate.delete(userKey);
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
    private String makeTokenKey(String token) {
        return "refresh:" + token;
    }

    /**
     * 유저 id와 키스페이스 값을 합쳐 uid key를 생성합니다.
     * @param userId
     * @return
     */
    private String makeUserIdKey(Long userId) {
        return "uid:" + userId;
    }
}
