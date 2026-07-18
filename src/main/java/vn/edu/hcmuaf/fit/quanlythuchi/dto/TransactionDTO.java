package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionDTO {
    private Long id;
    private Long parentId;
    private Long fundId;
    private Long categoryId;
    private Long partnerId;
    private Long userId;
    private String type;
    private String status;
    private Double amount;
    private String note;
    private String transactionCode;
    private Date transactionDate;
    private Date createdAt;
    private Boolean hasWarning;
    private String warningLevel;
    private String userName; // read-only: populated từ tx.getUser().getFullName()
    private String accompaniedBy;
    private String originalDocuments;
    private Long debtId; // null = giao dịch thường | có giá trị = đang thanh toán khoản nợ này
}