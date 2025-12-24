package seondays.shareticon.exception.business;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> context;

    protected BusinessException(String message, HttpStatus status, Map<String, Object> context) {
        super(message);
        this.status = status;

        Map<String, Object> enrichedContext = new LinkedHashMap<>();
        enrichedContext.put("class", this.getClass().getSimpleName());
        if (!context.isEmpty()) {
            enrichedContext.putAll(context);
        }
        this.context = enrichedContext;
    }

    protected BusinessException(String message, HttpStatus status) {
        this(message, status, Map.of());
    }
}
