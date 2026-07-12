package vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferRequest {
    private Long fromFundId;
    private Long toFundId;
    private Double amount;
    private String reason;
    private String note;
}
