package com.banking_system.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ApiError> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(LoanNotFoundException.class)
	public ResponseEntity<ApiError> handleLoanNotFound(LoanNotFoundException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(LoanEditNotAllowedException.class)
	public ResponseEntity<ApiError> handleLoanEditNotAllowed(LoanEditNotAllowedException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(StatusChangeNotAllowedException.class)
	public ResponseEntity<ApiError> handleStatusChangeNotAllowed(StatusChangeNotAllowedException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ApiError> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<ApiError> handleInvalidAmount(InvalidAmountException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Validation failed",
				request.getRequestURI(),
				errors);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				"Authentication failed",
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				"Access denied",
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
		log.warn("IllegalStateException: {}", ex.getMessage());
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception", ex);
		ApiError body = new ApiError(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"Unexpected error",
				request.getRequestURI(),
				null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
