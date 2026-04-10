package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FundResponseDTO {
    private String name;
    private String type;
    private String status;
    private Double initialBalance;
}
