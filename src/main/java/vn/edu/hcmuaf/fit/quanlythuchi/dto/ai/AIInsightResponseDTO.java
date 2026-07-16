package vn.edu.hcmuaf.fit.quanlythuchi.dto.ai;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponseDTO {
    private String status; // "SUCCESS" or "DEGRADED"
    private String cashFlowNarrative;
    private String cashFlowStatus; // "HEALTHY", "WARNING", "CRITICAL"
    private List<SpendingSpike> spendingSpikes;
    private List<String> recommendations;
    private LiquidityRisk liquidityRisk;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendingSpike {
        private String category;
        private Double overagePercent;
        private String comment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiquidityRisk {
        private Boolean hasRisk;
        private String message;
    }
}
