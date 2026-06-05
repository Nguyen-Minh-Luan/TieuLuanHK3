package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long id;
    private String title;
    private String type;         // MONTHLY, QUARTERLY, YEARLY, CUSTOM
    private Date fromDate;
    private Date toDate;
    private String note;
    private String status;       // DRAFT, PUBLISHED
    private Long createdBy;      // userId
    private Date createdAt;
    private Date updatedAt;
//  --------Response Only----------
    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;
    /**
     * Danh sách giao dịch ACTIVE trong kỳ báo cáo.
     * Chỉ có giá trị khi gọi GET /reports/{id} hoặc POST /reports/{id}/recalculate.
     * Null ở các response tạo/cập nhật/danh sách để giữ response gọn.
     */
    private List<TransactionDTO> transactions;
    private Integer transactionCount;
}