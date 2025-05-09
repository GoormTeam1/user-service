package edu.goorm.userservice.global.exception;

import edu.goorm.userservice.global.exception.BusinessException;
import edu.goorm.userservice.global.exception.ErrorCode;
import edu.goorm.userservice.global.response.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
	}

	// 예상 못한 모든 예외 처리 (로그 추가됨!)
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
		log.error("예기치 못한 오류", e); // ← 추가된 부분
		return ResponseEntity
				.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(ApiResponse.error(
						ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
						ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
				));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<Object>> handleValidationException(
			MethodArgumentNotValidException e) {
		String message = e.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.findFirst()
				.orElse("잘못된 요청입니다.");

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(HttpStatus.BAD_REQUEST, message));
	}
}
