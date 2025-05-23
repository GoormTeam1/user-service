package edu.goorm.userservice.global.exception;

import edu.goorm.userservice.global.logger.CustomLogger;
import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import edu.goorm.userservice.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e,
      HttpServletRequest request) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("비즈니스 예외 발생: {}", errorCode.getMessage());
    int status = errorCode.getStatus().value();
    CustomLogger.logError(
        request.getRequestURI(),
        request.getMethod(),
        e,
        status
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
    log.error("예기치 못한 오류 발생", e);

    CustomLogger.logError(
        request.getRequestURI(),
        request.getMethod(),
        e,
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );

    return ResponseEntity
        .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ApiResponse<Object>> handleValidationException(
      MethodArgumentNotValidException e, HttpServletRequest request) {

    String message = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .findFirst()
        .orElse("잘못된 요청입니다.");

    log.warn("유효성 검사 실패: {}", message);

    CustomLogger.logError(
        request.getRequestURI(),
        request.getMethod(),
        e, HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, message));
  }
}
