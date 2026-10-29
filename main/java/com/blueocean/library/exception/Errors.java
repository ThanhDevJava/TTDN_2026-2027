package com.blueocean.library.exception;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errors {
  public static <T> T required(java.util.Optional<T> item) {
    return item.map(com.blueocean.library.service.Views::loaded).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"));
  }

  public static void check(boolean ok, String message) {
    if (!ok) throw new ResponseStatusException(HttpStatus.CONFLICT, message);
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(Map.of("message", "Mã bị trùng hoặc dữ liệu đang được sử dụng"));
  }

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  ResponseEntity<?> invalid(Exception e) {
    return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu đầu vào không hợp lệ"));
  }

  @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
  ResponseEntity<?> concurrent(Exception e) {
    return ResponseEntity.status(409)
        .body(Map.of("message", "Dữ liệu vừa thay đổi, vui lòng thử lại"));
  }
}
