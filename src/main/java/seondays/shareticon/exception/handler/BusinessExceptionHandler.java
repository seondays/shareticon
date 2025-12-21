package seondays.shareticon.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seondays.shareticon.exception.business.AlreadyAppliedToGroupException;
import seondays.shareticon.exception.business.BusinessException;
import seondays.shareticon.exception.business.ExpiredVoucherException;
import seondays.shareticon.exception.business.GroupCreateException;
import seondays.shareticon.exception.business.GroupNotFoundException;
import seondays.shareticon.exception.business.GroupUserNotFoundException;
import seondays.shareticon.exception.business.IllegalOAuthProviderException;
import seondays.shareticon.exception.business.IllegalStatus;
import seondays.shareticon.exception.business.IllegalVoucherImageException;
import seondays.shareticon.exception.business.ImageUploadException;
import seondays.shareticon.exception.business.InvalidAccessException;
import seondays.shareticon.exception.business.InvalidGroupLeaderException;
import seondays.shareticon.exception.business.InvalidJoinGroupException;
import seondays.shareticon.exception.business.InvalidVoucherDeleteException;
import seondays.shareticon.exception.business.InvalidVoucherExpireException;
import seondays.shareticon.exception.business.PresignedUrlGenerationException;
import seondays.shareticon.exception.business.UserNotFoundException;
import seondays.shareticon.exception.business.VoucherNotFoundException;
import seondays.shareticon.exception.CustomExceptionResponse;

@Slf4j
@RestControllerAdvice
public class BusinessExceptionHandler {

    @ExceptionHandler({
            IllegalStatus.class,
            ExpiredVoucherException.class,
            IllegalOAuthProviderException.class,
            IllegalVoucherImageException.class,
            AlreadyAppliedToGroupException.class,
            InvalidJoinGroupException.class,
            GroupNotFoundException.class,
            UserNotFoundException.class,
            VoucherNotFoundException.class,
            GroupUserNotFoundException.class,
            InvalidAccessException.class,
            InvalidVoucherDeleteException.class,
            InvalidGroupLeaderException.class,
            InvalidVoucherExpireException.class})
    public ResponseEntity<CustomExceptionResponse> handleBusinessException(BusinessException e) {
        log.warn("[BUSINESS] --- {}", e.getMessage());
        return createExceptionResponse(e.getMessage(), e.getStatus());
    }

    @ExceptionHandler({
            ImageUploadException.class,
            GroupCreateException.class,
            PresignedUrlGenerationException.class})
    public ResponseEntity<CustomExceptionResponse> handleSystemException(BusinessException e) {
        log.error("[SYSTEM] --- ", e);
        return createExceptionResponse(e.getMessage(), e.getStatus());
    }

    private ResponseEntity<CustomExceptionResponse> createExceptionResponse(
            String message, HttpStatus status) {
        CustomExceptionResponse response = CustomExceptionResponse.of(message, status.value());
        return new ResponseEntity<>(response, status);
    }
}
