package seondays.shareticon.login.token;

import java.util.Arrays;

public enum TokenType {
    REFRESH,
    ACCESS;

    public static boolean isRefreshToken(TokenType tokenType) {
        return REFRESH.equals(tokenType);
    }

    public static TokenType of(String tokenType) {
        return Arrays.stream(TokenType.values())
                .filter(enumTokenType -> enumTokenType.name().equalsIgnoreCase(tokenType))
                .findFirst()
                .orElseThrow();
    }
}
