package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private String title;
    private String type;         // MONTHLY, QUARTERLY, YEARLY, CUSTOM
    private Date fromDate;
    private Date toDate;
    private String note;
    private String status;       // DRAFT, PUBLISHED
    private Long createdBy;      // userId
    private String createdByName;  // Tên người tạo báo cáo
    private Date createdAt;
    private Date updatedAt;
    
    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;

    // Phần Tài Sản
    private Double cashAndEquivalents;       // Mã 110: Tiền
    private Double accountsReceivable;       // Mã 120: Nợ phải thu
    private Double totalAssets;              // Mã 200: Tổng tài sản

    // Phần Nguồn Vốn
    private Double accountsPayable;          // Mã 310: Nợ phải trả
    private Double taxPayable;               // Mã 320: Thuế phải nộp
    private Double totalLiabilities;         // Mã 300: Tổng nợ phải trả
    private Double ownerEquity;              // Mã 410: Vốn đầu tư chủ sở hữu
    private Double totalEquity;              // Mã 400: Vốn chủ sở hữu
    private Double totalLiabilitiesAndEquity;// Mã 500: Tổng nguồn vốn

    // Số đầu năm (Beginning of Year)
    private Double cashAndEquivalentsBoy;
    private Double accountsReceivableBoy;
    private Double totalAssetsBoy;
    private Double accountsPayableBoy;
    private Double taxPayableBoy;
    private Double totalLiabilitiesBoy;
    private Double ownerEquityBoy;
    private Double totalEquityBoy;
    private Double totalLiabilitiesAndEquityBoy;

    private List<TransactionDTO> transactions;
    private Integer transactionCount;
}
