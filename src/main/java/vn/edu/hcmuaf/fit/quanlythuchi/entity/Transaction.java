package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_id")
    private Fund fund;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categories_id")
    private Category categories;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id")
    private Partner partner;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "parent_id")
    private Long parentId;
    private String transaction_code;
    private Date transaction_date;
    private String type;// có 4 loại là INCOME, EXPENSE, INCOME_DEBT, EXPENSE_DEBT
    private Double amount;
    private String amountInWord;
    private String note;
    private String status;
    private Date created_at;
    private Date datetime;
    private String reason;
    @Column(name = "accompanied_by")
    private String accompaniedBy;
    @Column(name = "original_documents")
    private String originalDocuments;
    @Column(name = "has_warning")
    private Boolean hasWarning = false;
    /** null = giao dịch thường, not null = phiếu đang thanh toán khoản nợ này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id")
    private Debt debt;
}
