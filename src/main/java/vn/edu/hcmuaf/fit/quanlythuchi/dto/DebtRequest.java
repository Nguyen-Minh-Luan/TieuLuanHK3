package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;
import java.util.Date;

/**
 * DebtRequest — Dữ liệu client gửi lên khi TẠO hoặc CẬP NHẬT khoản nợ.
 *
 * Chỉ chứa field client thực sự cần gửi.
 * Các field server tự tính (paidAmount, isPaid, paymentDate, remainingAmount,
 * partnerName, categoryName, createdByName...) không được phép set từ client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtRequest {

    /** Ngày ghi sổ nợ (yyyy-MM-dd). Nếu null, server dùng ngày hiện tại. */
    private Date debtDate;

    /** Loại nợ: "RECEIVABLE" (phải thu) | "PAYABLE" (phải chi). Bắt buộc khi tạo. */
    private String debtType;

    /** Tổng số tiền nợ. Bắt buộc khi tạo; > 0. */
    private Double totalAmount;

    /** ID đối tác liên quan. Bắt buộc khi tạo. */
    private Long partnerId;

    /**
     * ID hạng mục danh mục. Tùy chọn.
     * (Validate tồn tại bên service nếu được cung cấp.)
     */
    private Long categoryId;

    /**
     * ID người lập phiếu nợ. Bắt buộc khi tạo.
     * TODO: Sau khi tích hợp JWT, lấy từ SecurityContext thay vì client gửi lên.
     */
    private Long userId;

    /** Tiêu đề / nội dung ngắn của khoản nợ. Bắt buộc khi tạo. */
    private String title;

    /** Ghi chú nội bộ. Tùy chọn. */
    private String note;
}
