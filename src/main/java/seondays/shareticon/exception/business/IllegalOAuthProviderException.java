package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class IllegalOAuthProviderException extends BusinessException {

    public IllegalOAuthProviderException() {
        super("해당 로그인 제공자는 지원되지 않습니다", HttpStatus.BAD_REQUEST);
    }
}
