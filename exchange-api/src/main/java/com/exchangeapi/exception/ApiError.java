package com.exchangeapi.exception;

import org.springframework.http.HttpStatus;

public record ApiError(String message, HttpStatus status) {

	public static ApiError of(String message) {
		return new ApiError(message, null);
	}

}
