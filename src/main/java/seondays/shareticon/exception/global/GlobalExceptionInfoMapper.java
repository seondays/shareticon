package seondays.shareticon.exception.global;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

public class GlobalExceptionInfoMapper {

    public static GlobalExceptionInfo toExceptionInfo(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex) {
            String message = ex.getBindingResult().getAllErrors().isEmpty() ? "입력값이 유효하지 않습니다"
                    : ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
            return GlobalExceptionInfo.of(
                    message,
                    HttpStatus.BAD_REQUEST);
        }
        if (e instanceof MethodArgumentTypeMismatchException ex) {
            Class<?> requiredType = ex.getRequiredType();
            String typeName = requiredType != null ? requiredType.getSimpleName() : "올바른";

            return GlobalExceptionInfo.of(String.format(
                    "파라미터 '%s'의 값은 %s 타입이어야 합니다",
                    ex.getName(),
                    typeName), HttpStatus.BAD_REQUEST);
        }
        if (e instanceof MissingServletRequestParameterException ex) {
            return GlobalExceptionInfo.of(String.format(
                    "필수 파라미터 '%s'가 누락되었습니다",
                    ex.getParameterName()), HttpStatus.BAD_REQUEST);
        }
        if (e instanceof MultipartException) {
            return GlobalExceptionInfo.of(
                    "해당 요청은 multipart/form-data 형식으로만 지원됩니다",
                    HttpStatus.BAD_REQUEST);
        }
        if (e instanceof ConstraintViolationException ex) {
            return GlobalExceptionInfo.of(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        if (e instanceof InsufficientAuthenticationException) {
            return GlobalExceptionInfo.of("로그인이 필요합니다", HttpStatus.UNAUTHORIZED);
        }
        if (e instanceof AuthenticationException) {
            return GlobalExceptionInfo.of("유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED);
        }
        if (e instanceof NoResourceFoundException) {
            return GlobalExceptionInfo.of("요청에 대한 리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        }
        return GlobalExceptionInfo.of("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
