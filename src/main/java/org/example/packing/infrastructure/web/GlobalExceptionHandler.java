package org.example.packing.infrastructure.web;

import jakarta.validation.ConstraintViolationException;
import org.example.packing.domain.exception.BusinessRuleViolationException;
import org.example.packing.domain.exception.NotFoundException;
import org.example.packing.infrastructure.web.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
			final ConstraintViolationException exception
	) {
		final String message = exception.getConstraintViolations()
				.stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.sorted()
				.collect(Collectors.joining(", "));

		return ResponseEntity
				.badRequest()
				.body(ApiResponse.failure(message));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
			final HandlerMethodValidationException exception
	) {
		final String message = exception.getAllErrors()
				.stream()
				.map(MessageSourceResolvable::getDefaultMessage)
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
		log.error("Unhandled exception", exception);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.failure("Internal server error."));
	}

	private String formatFieldError(final FieldError fieldError) {
		return fieldError.getField() + ": " + fieldError.getDefaultMessage();
	}
}
