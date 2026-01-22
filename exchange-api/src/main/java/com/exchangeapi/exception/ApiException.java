package com.exchangeapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

	private final ApiError apiError;

	public ApiException(ApiError apiError) {
		this.apiError = apiError;
	}

	public static ApiException of(String message, HttpStatus status) {
		return new ApiException(new ApiError(message, status));
	}

	public static ApiException entityNotFound(String message) {
		return new ApiException(new ApiError(message, HttpStatus.NOT_FOUND));
	}

}
