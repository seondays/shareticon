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

    public static GlobalExceptionInfo toExceptionInfo(MethodArgumentNotValidException e) {
        return GlobalExceptionInfo.of(
                e.getBindingResult().getAllErrors().getFirst().getDefaultMessage(),
                HttpStatus.BAD_REQUEST);
    }

    public static GlobalExceptionInfo toExceptionInfo(MethodArgumentTypeMismatchException e) {
        return GlobalExceptionInfo.of(String.format(
                "파라미터 '%s'의 값은 %s 타입이어야 합니다",
                e.getName(),
                e.getRequiredType().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

    public static GlobalExceptionInfo toExceptionInfo(MissingServletRequestParameterException e) {
        return GlobalExceptionInfo.of(String.format(
                "필수 파라미터 '%s'가 누락되었습니다",
                e.getParameterName()), HttpStatus.BAD_REQUEST);
    }

    public static GlobalExceptionInfo toExceptionInfo(InsufficientAuthenticationException e) {
        return GlobalExceptionInfo.of("로그인이 필요합니다", HttpStatus.UNAUTHORIZED);
    }

    public static GlobalExceptionInfo toExceptionInfo(AuthenticationException e) {
        return GlobalExceptionInfo.of("유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED);
    }

    public static GlobalExceptionInfo toExceptionInfo(MultipartException e) {
        return GlobalExceptionInfo.of(
                "해당 요청은 multipart/form-data 형식으로만 지원됩니다. 파일을 첨부하여 다시 시도해주세요",
                HttpStatus.BAD_REQUEST);
    }

    public static GlobalExceptionInfo toExceptionInfo(ConstraintViolationException e) {
        return GlobalExceptionInfo.of(
                e.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    public static GlobalExceptionInfo toExceptionInfo(NoResourceFoundException e) {
        return GlobalExceptionInfo.of("요청에 대한 리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND);
    }

    public static GlobalExceptionInfo toExceptionInfo(Exception e) {
        return GlobalExceptionInfo.of("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
