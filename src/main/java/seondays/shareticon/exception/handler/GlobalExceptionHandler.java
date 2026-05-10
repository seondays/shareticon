package seondays.shareticon.exception.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import seondays.shareticon.exception.global.GlobalExceptionInfo;
import seondays.shareticon.exception.global.GlobalExceptionInfoMapper;
import seondays.shareticon.exception.CustomExceptionResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MultipartException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class})
    public ResponseEntity<CustomExceptionResponse> handleValidationException(Exception e) {
        log.warn("[BUSINESS] --- {}", e.getMessage());
        return createExceptionResponse(GlobalExceptionInfoMapper.toExceptionInfo(e));
    }

    @ExceptionHandler({
            AuthenticationException.class,
            InsufficientAuthenticationException.class})
    public ResponseEntity<CustomExceptionResponse> handleAuthenticationException(Exception e) {
        log.debug("[AUTH] ---", e);
        return createExceptionResponse(GlobalExceptionInfoMapper.toExceptionInfo(e));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {
        log.debug("[UNKNOWN_PATH] --- {}", e.getResourcePath());
        return createExceptionResponse(GlobalExceptionInfoMapper.toExceptionInfo(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionResponse> handleGeneralException(Exception e) {
        log.error("[SYSTEM] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        return createExceptionResponse(GlobalExceptionInfoMapper.toExceptionInfo(e));
    }

    private ResponseEntity<CustomExceptionResponse> createExceptionResponse(
            GlobalExceptionInfo globalExceptionInfo) {
        CustomExceptionResponse response = CustomExceptionResponse.of(globalExceptionInfo);
        return new ResponseEntity<>(response, globalExceptionInfo.getStatus());
    }
}
