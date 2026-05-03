package vn.edu.hcmuaf.fit.quanlythuchi.config;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp hỗ trợ cấu trúc phản hồi API thống nhất cho toàn hệ thống.
 * Kết hợp giữa ResponseEntity (HTTP Status) và Body (Dữ liệu tùy chỉnh).
 * * @param <T> Kiểu dữ liệu của nội dung phản hồi (payload).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private String errorCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(HttpStatus httpStatus, String message, T data, String errorCode) {
        this.status = httpStatus.is2xxSuccessful() ? "success" : "error";
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // 1. Thành công 200 OK (Mặc định)
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Thao tác thành công", data, null));
    }

    // 2. Thành công 200 OK + Message riêng
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, message, data, null));
    }

    // 3. Khởi tạo thành công 201 Created (Dùng cho Register/Create)
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, message, data, null));
    }

    // 4. Lỗi phía Client 400 Bad Request
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, String errorCode) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST, message, null, errorCode));
    }

    // 5. Lỗi xác thực 401 Unauthorized (Sai pass, hết hạn token)
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, message, null, "AUTH_UNAUTHORIZED"));
    }

    // 6. Lỗi hệ thống 500 Internal Server Error
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, message, null, "SERVER_ERROR"));
    }
}