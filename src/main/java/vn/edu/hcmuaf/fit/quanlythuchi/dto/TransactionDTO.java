package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionDTO {
    private Long fundId;
    private Long categoryId;
    private Long partnerId;
    private Long userId;
    private String type; // "INCOME" hoặc "EXPENSE"
    private Double amount;
    private String note;
}