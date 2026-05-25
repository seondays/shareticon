package seondays.shareticon.warmup;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "warmup")
public record WarmupProperties(
        String token,
        Long userId,
        Long groupId,
        Integer pageSize
) {

    public int actualPageSize() {
        if (pageSize == null) {
            return 10;
        }
        if (pageSize < 1) {
            return 1;
        }
        if (pageSize > 20) {
            return 20;
        }
        return pageSize;
    }

    public boolean hasTarget() {
        return userId != null && groupId != null;
    }

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }
}
