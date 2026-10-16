package org.example.packing.application.controller;

import jakarta.validation.ConstraintViolationException;
import org.example.packing.application.dto.ApiResponse;
import org.example.packing.domain.exception.BusinessRuleViolationException;
import org.example.packing.domain.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Translates exceptions into HTTP responses. Lives in the web layer (it depends
 * on Spring Web), and reacts to the semantic domain exception types, so a new
 * "not found" or "rule violated" exception needs no change here.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
			final NotFoundException exception
	) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.failure(exception.getMessage()));
	}

	@ExceptionHandler(BusinessRuleViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessRuleViolationException(
			final BusinessRuleViolationException exception
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

	/**
	 * Constraint annotations on controller method parameters ({@code @Min},
	 * {@code @Max} on {@code @RequestParam}): {@code @Validated} controllers
	 * raise {@link ConstraintViolationException}, Spring MVC's built-in method
	 * validation raises {@link HandlerMethodValidationException}.
	 */
	@ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
	public ResponseEntity<ApiResponse<Void>> handleParameterValidationException(
			final Exception exception
	) {
		return ResponseEntity
				.badRequest()
				.body(ApiResponse.failure("Invalid request parameters."));
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
