package vn.edu.hcmuaf.fit.quanlythuchi.dto.ai;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSnapshotDTO {
    private String period; // e.g. "2026-02 → 2026-07"
    private List<Map<String, Object>> monthlyCashFlow;
    private List<CategoryTrendDTO> categorySpikes;
    private List<DebtAlertDTO> debtAlerts;
    private Map<String, Double> summary;
}
