package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chart_of_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccount {
    
    @Id
    @Column(nullable = false, length = 10)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "account_group")
    private AccountGroup group;
}
