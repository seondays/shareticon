package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidVoucherDeleteException extends BusinessException {

    private InvalidVoucherDeleteException(Map<String, Object> context) {
        super("쿠폰을 등록한 유저만 쿠폰 삭제가 가능합니다", HttpStatus.FORBIDDEN, context);
    }

    public static InvalidVoucherDeleteException of(Long voucherId) {
        return new InvalidVoucherDeleteException(Map.of("voucherId", voucherId));
    }
}
