package vn.edu.hcmuaf.fit.quanlythuchi.dto.ai;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtAlertDTO {
    private String partner;
    private String type;
    private Double remaining;
    private Long daysUntilDue;
}
