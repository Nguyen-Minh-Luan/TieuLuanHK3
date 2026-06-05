package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionDTO {
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
    private String reason;
    private String accompaniedBy;
    private String originalDocuments;
}