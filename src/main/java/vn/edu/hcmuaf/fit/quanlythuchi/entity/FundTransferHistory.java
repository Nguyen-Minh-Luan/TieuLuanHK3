package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_transfer_history", indexes = {
    @Index(name = "idx_transfer_from_fund", columnList = "from_fund_id"),
    @Index(name = "idx_transfer_to_fund", columnList = "to_fund_id"),
    @Index(name = "idx_transfer_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_code", length = 50, nullable = false)
    private String transferCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_fund_id", nullable = false)
    private Fund fromFund;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_fund_id", nullable = false)
    private Fund toFund;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "amount_in_word", length = 500)
    private String amountInWord;

    @Column(length = 1000, nullable = false)
    private String reason;

    @Column(length = 1000)
    private String note;

    @Column(length = 20, nullable = false)
    private String status; // SUCCESS, FAILED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "from_fund_balance_after")
    private Double fromFundBalanceAfter;

    @Column(name = "to_fund_balance_after")
    private Double toFundBalanceAfter;
}
