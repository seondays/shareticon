package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class VoucherNotFoundException extends BusinessException {

    private final Long voucherId;

    public VoucherNotFoundException(Long voucherId) {
        super("해당 쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        this.voucherId = voucherId;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "voucherId", voucherId
        );
    }
}
