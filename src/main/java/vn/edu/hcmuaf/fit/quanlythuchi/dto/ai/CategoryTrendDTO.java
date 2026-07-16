package vn.edu.hcmuaf.fit.quanlythuchi.dto.ai;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTrendDTO {
    private String category;
    private Double currentMonth;
    private Double historicalAverage;
    private Double overagePercent;
}
