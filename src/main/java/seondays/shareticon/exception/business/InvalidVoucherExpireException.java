package seondays.shareticon.exception.business;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidVoucherExpireException extends BusinessException {

    private final LocalDate expiration;

    public InvalidVoucherExpireException(LocalDate expiration) {
        super("쿠폰 만료일은 오늘보다 이전 날짜로 지정할 수 없습니다", HttpStatus.BAD_REQUEST);
        this.expiration = expiration;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "expiration", expiration.toString()
        );
    }
}
