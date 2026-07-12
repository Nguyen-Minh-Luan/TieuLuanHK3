package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "fund_reconciliations", indexes = {
    @Index(name = "idx_recon_fund_period", columnList = "fund_id, period_start, period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FundReconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "group_code", length = 50)
    private String groupCode;          // nhóm các phiên kiểm kê tạo cùng lúc (nhiều quỹ)

    @Column(name = "period_start", nullable = false)
    private Date periodStart;

    @Column(name = "period_end", nullable = false)
    private Date periodEnd;

    @Column(name = "opening_balance_system", nullable = false)
    private Double openingBalanceSystem;

    @Column(name = "closing_balance_system", nullable = false)
    private Double closingBalanceSystem;

    @Column(name = "actual_balance")
    private Double actualBalance;      // Thủ quỹ nhập tay

    private Double difference;         // = actualBalance - closingBalanceSystem

    @Column(nullable = false)
    private String status;             // DRAFT, CLOSED, REOPENED

    @Column(length = 1000)
    private String note;               // lý do chênh lệch (bắt buộc nếu difference != 0)

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "closed_by")
    private User closedBy;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne
    @JoinColumn(name = "reopened_by")
    private User reopenedBy;
    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;
    @Column(name = "reopen_reason", length = 500)
    private String reopenReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
