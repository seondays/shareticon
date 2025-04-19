package seondays.shareticon.exception;

public class IllegalOAuthProviderException extends RuntimeException {

    public IllegalOAuthProviderException() {
        super("해당 로그인 제공자는 지원되지 않습니다.");
    }
}
