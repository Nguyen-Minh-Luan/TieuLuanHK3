package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDTO {
    private Long id;
    private String name;
    private String type;
    private String email;
}