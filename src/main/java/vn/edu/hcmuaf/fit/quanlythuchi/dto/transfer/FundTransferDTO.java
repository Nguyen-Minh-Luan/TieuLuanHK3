package vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferDTO {
    private Long id;
    private String transferCode;
    
    private Long fromFundId;
    private String fromFundName;
    
    private Long toFundId;
    private String toFundName;
    
    private Double amount;
    private String amountInWord;
    private String reason;
    private String note;
    private String status;
    
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    
    private Double fromFundBalanceAfter;
    private Double toFundBalanceAfter;
}
