package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

/**
 * DebtResponse — Dữ liệu server trả về cho client.
 *
 * Dùng cho tất cả phản hồi GET, cũng như kết quả trả về sau POST / PATCH.
 * Field payments chỉ được populate khi GET /debts/{id} (chi tiết đơn lẻ).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtResponse {

    private Long id;

    // ── Thông tin cốt lõi ─────────────────────────────────────────
    private Date debtDate;
    private String debtType;        // "RECEIVABLE" | "PAYABLE"
    private Double totalAmount;

    // ── Trạng thái thanh toán (server tự tính / tự set) ──────────
    private Double paidAmount;
    private Boolean isPaid;
    private Date paymentDate;
    private Double remainingAmount; // = totalAmount - paidAmount

    // ── Liên kết (ID gốc + tên hiển thị) ─────────────────────────
    private Long partnerId;
    private String partnerName;

    private Long categoryId;
    private String categoryName;

    private Long userId;
    private String createdByName;

    // ── Metadata ──────────────────────────────────────────────────
    private String title;
    private String note;
    private Date createdAt;
    private Date updatedAt;

    // ── Lịch sử thanh toán — chỉ populate khi GET /debts/{id} ────
    private List<TransactionDTO> payments;
}
