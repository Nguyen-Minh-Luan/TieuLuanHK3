package vn.edu.hcmuaf.fit.quanlythuchi.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Bắt tất cả exception ném ra từ Service layer,
 * chuyển thành ApiResponse lỗi thống nhất thay vì để Spring
 * trả về trang lỗi HTML mặc định.
 *
 * Controller KHÔNG cần try/catch nữa — cứ để exception lan ra,
 * handler này sẽ xử lý tập trung.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 400 Bad Request
	 * Ném khi dữ liệu đầu vào không hợp lệ (validate thất bại).
	 * Ví dụ: IllegalArgumentException("Số tiền phải lớn hơn 0")
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		return ApiResponse.error(ex.getMessage(), "INVALID_INPUT");
	}

	/**
	 * 400 Bad Request
	 * Ném khi trạng thái không cho phép thực hiện hành động.
	 * Ví dụ: IllegalStateException("Nguồn tiền đã bị xóa, không thể cập nhật")
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
		return ApiResponse.error(ex.getMessage(), "INVALID_STATE");
	}

	/**
	 * 404 Not Found
	 * Ném khi không tìm thấy dữ liệu trong DB.
	 * Ví dụ: RuntimeException("Không tìm thấy nguồn tiền với ID: 5")
	 *
	 * Lưu ý: RuntimeException là base class nên đặt sau cùng để không
	 * nuốt mất các exception con phía trên.
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage(), null, "NOT_FOUND"));
	}

	/**
	 * 500 Internal Server Error
	 * Bắt tất cả exception không lường trước.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		return ApiResponse.internalServerError("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau");
	}
}