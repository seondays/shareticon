package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidVoucherDeleteException extends BusinessException {

    private final Long voucherId;

    public InvalidVoucherDeleteException(Long voucherId) {
        super("쿠폰을 등록한 유저만 쿠폰 삭제가 가능합니다", HttpStatus.FORBIDDEN);
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
