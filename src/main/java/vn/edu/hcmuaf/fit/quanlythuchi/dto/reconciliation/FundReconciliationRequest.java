package vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FundReconciliationRequest {
    private List<Long> fundIds;
    private Date periodStart;
    private Date periodEnd;
}
