package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccountDTO {
    private String code;
    private String name;
    private String group;
}
