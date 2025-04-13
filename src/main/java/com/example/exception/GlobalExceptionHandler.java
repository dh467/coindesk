package com.example.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 外部 API 呼叫錯誤
   */
  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException e) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Failed to retrieve data from external API.");
  }

  /**
   * 請求參數驗證失敗
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("message", "Invalid input");
    response.put("errors", errors);

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 找不到資源
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /**
   * 預設錯誤處理
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "Unexpected error occurred. Please try again later.");
  }

  /**
   * 共用格式回傳方法
   */
  private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status,
      String message) {
    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("status", status.value());
    errorBody.put("message", message);
    return ResponseEntity.status(status).body(errorBody);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    if (cause instanceof UnrecognizedPropertyException) {
      String field = ((UnrecognizedPropertyException) cause).getPropertyName();
      return ResponseEntity.badRequest().body("Request contains undefined field: " + field);
    }

    return ResponseEntity.badRequest().body("Malformed JSON request");
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateKey(DuplicateKeyException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", e.getMessage());
    body.put("status", HttpStatus.CONFLICT.value());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }
}
