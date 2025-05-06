package seondays.shareticon.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import seondays.shareticon.exception.AlreadyAppliedToGroupException;
import seondays.shareticon.exception.ExpiredVoucherException;
import seondays.shareticon.exception.GroupCreateException;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.IllegalOAuthProviderException;
import seondays.shareticon.exception.IllegalVoucherImageException;
import seondays.shareticon.exception.ImageUploadException;
import seondays.shareticon.exception.InvalidAccessVoucherException;
import seondays.shareticon.exception.InvalidVoucherDeleteException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ExpiredVoucherException.class)
    public ResponseEntity<CustomExceptionResponse> handleExpiredVoucherException(
            ExpiredVoucherException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleGroupNotFoundException(
            GroupNotFoundException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(IllegalOAuthProviderException.class)
    public ResponseEntity<CustomExceptionResponse> handleIllegalOAuthProviderException(
            IllegalOAuthProviderException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<CustomExceptionResponse> handleImageUploadException(
            ImageUploadException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAccessVoucherException.class)
    public ResponseEntity<CustomExceptionResponse> handleInvalidAccessVoucherException(
            InvalidAccessVoucherException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidVoucherDeleteException.class)
    public ResponseEntity<CustomExceptionResponse> handleInvalidVoucherDeleteException(
            InvalidVoucherDeleteException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalVoucherImageException.class)
    public ResponseEntity<CustomExceptionResponse> handleIllegalVoucherImageException(
            IllegalVoucherImageException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleUserNotFoundException(
            UserNotFoundException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleVoucherNotFoundException(
            VoucherNotFoundException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyAppliedToGroupException.class)
    public ResponseEntity<CustomExceptionResponse> handleAlreadyAppliedToGroupException(
            AlreadyAppliedToGroupException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GroupCreateException.class)
    public ResponseEntity<CustomExceptionResponse> handleGroupCreateException(
            GroupCreateException e) {
        log.error(String.valueOf(e));

        return createExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomExceptionResponse> bindException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        log.error(String.valueOf(e));

        return createExceptionResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomExceptionResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error(String.valueOf(e));

        String parameterName = e.getName();
        String requiredTypeName = e.getRequiredType().getSimpleName();
        String message = String.format("파라미터 '%s'의 값은 %s 타입이어야 합니다", parameterName, requiredTypeName);
        return createExceptionResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomExceptionResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error(String.valueOf(e));

        String parameterName = e.getParameterName();
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다", parameterName);
        return createExceptionResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<CustomExceptionResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException e) {
        log.error(String.valueOf(e));

        String message = "로그인이 필요합니다";
        return createExceptionResponse(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomExceptionResponse> handleIInvalidBearerTokenException(
            AuthenticationException e) {
        log.error("", e);

        String message = "유효하지 않은 토큰입니다";
        return createExceptionResponse(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<CustomExceptionResponse> handleMultipartException(
            MultipartException e) {
        log.error(String.valueOf(e));

        String message = "해당 요청은 multipart/form-data 형식으로만 지원됩니다. 파일을 첨부하여 다시 시도해주세요";
        return createExceptionResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionResponse> generalException(Exception e) {
        log.error("{} : {}", e.getMessage(), e);

        final String message = "서버 오류가 발생했습니다";
        return createExceptionResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<CustomExceptionResponse> createExceptionResponse(String message,
            HttpStatus status) {
        CustomExceptionResponse response = CustomExceptionResponse.of(message, status.value());
        return new ResponseEntity<>(response, status);
    }
}
