package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private BigDecimal budgeting;
    private Integer tax;
    private Long parentId;
    private List<CategoryDTO> children;
}