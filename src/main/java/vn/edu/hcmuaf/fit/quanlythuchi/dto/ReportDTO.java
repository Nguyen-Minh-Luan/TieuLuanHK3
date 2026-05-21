package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

import java.util.Date;

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
    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;
    private String note;
    private String status;       // DRAFT, PUBLISHED
    private Long createdBy;      // userId
    private Date createdAt;
    private Date updatedAt;
}