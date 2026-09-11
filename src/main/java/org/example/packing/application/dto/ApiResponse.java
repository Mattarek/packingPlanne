package org.example.packing.application.dto;

public record ApiResponse<T>(
		boolean success,
		String message,
		T data
) {
	public static <T> ApiResponse<T> success(final String message, final T data) {
		return new ApiResponse<>(true, message, data);
	}

	public static <T> ApiResponse<T> failure(final String message, final T data) {
		return new ApiResponse<>(false, message, data);
	}

	public static ApiResponse<Void> failure(final String message) {
		return new ApiResponse<>(false, message, null);
	}
}