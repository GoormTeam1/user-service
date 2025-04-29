package edu.goorm.userservice.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
	private int status;  // HTTP Status 코드
	private String message;  // 결과 메시지
	private T data;  // 응답 데이터

	// 성공 응답용
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, "Success", data);
	}

	// 실패 응답용
	public static <T> ApiResponse<T> error(int status, String message) {
		return new ApiResponse<>(status, message, null);
	}
}
