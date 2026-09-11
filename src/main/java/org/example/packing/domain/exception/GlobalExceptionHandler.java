package org.example.packing.domain.exception;

import org.example.packing.application.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(VehicleNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleVehicleNotFoundException(
			final VehicleNotFoundException exception
	) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.failure(exception.getMessage()));
	}

	@ExceptionHandler(PackageNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handlePackageNotFoundException(
			final PackageNotFoundException exception
	) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.failure(exception.getMessage()));
	}

	@ExceptionHandler(PackageNotAcceptedException.class)
	public ResponseEntity<ApiResponse<Void>> handlePackageNotAcceptedException(
			final PackageNotAcceptedException exception
	) {
		return ResponseEntity
				.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ApiResponse.failure(exception.getMessage()));
	}

	@ExceptionHandler(PackageTooLargeException.class)
	public ResponseEntity<ApiResponse<Void>> handlePackageTooLargeException(
			final PackageTooLargeException exception
	) {
		return ResponseEntity
				.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ApiResponse.failure(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
			final MethodArgumentNotValidException exception
	) {
		final String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining(", "));

		return ResponseEntity
				.badRequest()
				.body(ApiResponse.failure(message));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
			final MethodArgumentTypeMismatchException exception
	) {
		final String message = "Invalid value for parameter: " + exception.getName();

		return ResponseEntity
				.badRequest()
				.body(ApiResponse.failure(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
			final HttpMessageNotReadableException exception
	) {
		return ResponseEntity
				.badRequest()
				.body(ApiResponse.failure("Invalid request body."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(
			final Exception exception
	) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.failure("Internal server error."));
	}

	private String formatFieldError(final FieldError fieldError) {
		return fieldError.getField() + ": " + fieldError.getDefaultMessage();
	}
}