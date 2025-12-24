package seondays.shareticon.exception.business;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidVoucherExpireException extends BusinessException {

    private InvalidVoucherExpireException(Map<String, Object> context) {
        super("쿠폰 만료일은 오늘보다 이전 날짜로 지정할 수 없습니다", HttpStatus.BAD_REQUEST, context);
    }

    public static InvalidVoucherExpireException of(LocalDate expiration) {
        return new InvalidVoucherExpireException(Map.of("expiration", expiration.toString()));
    }
}
