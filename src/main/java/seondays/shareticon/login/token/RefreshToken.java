package seondays.shareticon.login.token;

import java.time.Instant;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class RefreshToken {
    @Id
    private String token;
    private Long userId;
    private String expiration;
    private Long timeToLive;

    public static RefreshToken create(Long id, String token, Date expiration, Long timeToLive) {
        return RefreshToken.builder()
                .userId(id)
                .token(token)
                .expiration(String.valueOf(expiration))
                .timeToLive(timeToLive)
                .build();
    }
}
