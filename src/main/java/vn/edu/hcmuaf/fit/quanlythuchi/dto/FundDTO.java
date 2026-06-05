package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDTO {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Double initialBalance;
    private Double currentBalance;
    // XEM LẠI MẪU PHIẾU THU , CHI CỦA BỘ NHÀ NƯỚC ,
    //  XUẤT PHIẾU THU CHI THEO BỘ CỦA NHÀ NƯỚC , XUẤT BÁO CÁO THEO BỘ CỦA NHÀ NƯỚC



    // PHIẾU THU , PHIẾU CHI , PHIẾU TỒN
}
