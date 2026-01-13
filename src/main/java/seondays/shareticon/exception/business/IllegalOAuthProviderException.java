package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class IllegalOAuthProviderException extends BusinessException {

    private final String providerType;

    public IllegalOAuthProviderException(String providerType) {
        super("해당 로그인 제공자는 지원되지 않습니다", HttpStatus.BAD_REQUEST);
        this.providerType = providerType;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "providerType", providerType
        );
    }
}
