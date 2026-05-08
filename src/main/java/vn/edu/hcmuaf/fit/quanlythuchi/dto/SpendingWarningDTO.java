package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingWarningDTO {
    private boolean hasWarning;
    private String categoryName;
    private Double currentMonthTotal;
    private Double historicalAverage;
    private Double overagePercent;
    private String message;
    private String level; // "NORMAL", "WARNING", "CRITICAL"
}