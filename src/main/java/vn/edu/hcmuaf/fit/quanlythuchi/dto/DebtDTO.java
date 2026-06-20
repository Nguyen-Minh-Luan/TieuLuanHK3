package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtDTO {
    private Long id;

    private Date debtDate;
    private String debtType;        // "RECEIVABLE" | "PAYABLE"
    private Double totalAmount;
    private Double paidAmount;      // Response only — không nhận từ client khi tạo
    private Boolean isPaid;         // Response only
    private Date paymentDate;       // Response only

    // Request: gửi ID lên
    private Long partnerId;
    private Long categoryId;
    private Long userId;

    // Response: trả về tên để frontend hiển thị (không cần query thêm)
    private String partnerName;
    private String categoryName;
    private String createdByName;



    private String title;
    private String note;
    private Date createdAt;
    private Date updatedAt;

    // Số tiền còn phải trả/thu = totalAmount - paidAmount
    private Double remainingAmount;

    // Lịch sử thanh toán — chỉ populate khi GET /debts/{id}
    private List<TransactionDTO> payments;
}
