package seondays.shareticon.login.token;

public record StoredRefreshToken(Long id,
                                 String expiration) {
    public static StoredRefreshToken of(RefreshToken refreshToken) {
        return new StoredRefreshToken(refreshToken.getUserId(), refreshToken.getExpiration());
    }

}
