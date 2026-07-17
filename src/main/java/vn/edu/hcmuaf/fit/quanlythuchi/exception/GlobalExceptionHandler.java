package vn.edu.hcmuaf.fit.quanlythuchi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedPeriodException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedPeriodException(LockedPeriodException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ApiResponse<>(HttpStatus.LOCKED, ex.getMessage(), null, "PERIOD_LOCKED"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage(), null, "BAD_REQUEST"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage(), null, "NOT_FOUND"));
    }
}

