package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class VoucherNotFoundException extends BusinessException {

    private VoucherNotFoundException(Map<String, Object> context) {
        super("해당 쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND, context);
    }

    public static VoucherNotFoundException of(Long voucherId) {
        return new VoucherNotFoundException(Map.of("voucherId", voucherId));
    }
}
