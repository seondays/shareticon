package seondays.shareticon.exception.business;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class IllegalVoucherImageException extends BusinessException {

    private final String contentType;

    public IllegalVoucherImageException() {
        super("파일이 비어있거나 존재하지 않습니다", HttpStatus.BAD_REQUEST);
        this.contentType = null;
    }

    public IllegalVoucherImageException(String contentType) {
        super("올바른 이미지 파일 타입이 필요합니다", HttpStatus.BAD_REQUEST);
        this.contentType = contentType;
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("class", this.getClass().getSimpleName());
        if (contentType != null) {
            context.put("contentType", contentType);
        }
        return context;
    }
}
