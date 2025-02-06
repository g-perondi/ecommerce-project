package com.winet.ecommerce.exception;

import com.winet.ecommerce.exception.custom.ApiException;
import com.winet.ecommerce.exception.custom.ResourceNotFoundException;
import com.winet.ecommerce.payload.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ApiResponse> handleAllException(Exception exception) {
		ApiResponse apiResponse = new ApiResponse("Something went wrong...", false);
		return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = { ApiException.class })
	public ResponseEntity<ApiResponse> handleApiException(ApiException exception) {
		ApiResponse apiResponse = new ApiResponse(exception.getMessage(), false);
		return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = { ResourceNotFoundException.class })
	public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
		ApiResponse apiResponse = new ApiResponse(exception.getMessage(), false);
		return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = { MethodArgumentNotValidException.class })
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		Map<String, String> errorsMap = new HashMap<>();

		exception.getBindingResult().getAllErrors()
				.forEach(error -> {
					String fieldName = ((FieldError) error).getField();
					String errorMessage = error.getDefaultMessage();
					errorsMap.put(fieldName, errorMessage);
				});

		return new ResponseEntity<>(errorsMap, HttpStatus.BAD_REQUEST);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = { MissingServletRequestParameterException.class })
	public ResponseEntity<ApiResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
		ApiResponse apiResponse = new ApiResponse(exception.getMessage(), false);
		return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = { ConstraintViolationException.class })
	public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
		Map<String, String> errorsMap = new HashMap<>();

		exception.getConstraintViolations()
				.forEach(violation ->
						errorsMap.put(violation.getPropertyPath().toString(), violation.getMessage())
				);

		return new ResponseEntity<>(errorsMap, HttpStatus.BAD_REQUEST);
	}

}
