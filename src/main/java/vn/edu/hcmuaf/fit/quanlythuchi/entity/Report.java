package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "reports")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiêu đề báo cáo */
    @Column(nullable = false)
    private String title;

    /** Loại báo cáo: MONTHLY, QUARTERLY, YEARLY, CUSTOM */
    @Column(nullable = false)
    private String type;

    /** Ngày bắt đầu kỳ báo cáo */
    @Column(name = "from_date", nullable = false)
    private Date fromDate;

    /** Ngày kết thúc kỳ báo cáo */
    @Column(name = "to_date", nullable = false)
    private Date toDate;

    /** Tổng thu trong kỳ (được tính tổng hợp từ transactions) */
    @Column(name = "total_income")
    private Double totalIncome;

    /** Tổng chi trong kỳ */
    @Column(name = "total_expense")
    private Double totalExpense;

    /** Số dư cuối kỳ = totalIncome - totalExpense */
    @Column(name = "net_balance")
    private Double netBalance;

    /** Ghi chú thêm */
    private String note;

    /** Trạng thái: DRAFT, PUBLISHED */
    @Column(nullable = false)
    private String status;

    /** Người tạo báo cáo */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ── Phần Tài Sản ─────────────────────────────────────────────
    /** Mã 110: Tiền (tổng currentBalance của Fund tại thời điểm tạo báo cáo) */
    @Column(name = "cash_and_equivalents")
    private Double cashAndEquivalents;

    /** Mã 120: Nợ phải thu (RECEIVABLE chưa thanh toán trong kỳ) */
    @Column(name = "accounts_receivable")
    private Double accountsReceivable;

    /** Mã 200: Tổng tài sản = 110 + 120 */
    @Column(name = "total_assets")
    private Double totalAssets;

    // ── Phần Nguồn Vốn ───────────────────────────────────────────
    /** Mã 310: Nợ phải trả (PAYABLE chưa thanh toán trong kỳ) */
    @Column(name = "accounts_payable")
    private Double accountsPayable;

    /** Mã 320: Thuế phải nộp (EXPENSE thuộc category có tax > 0 trong kỳ) */
    @Column(name = "tax_payable")
    private Double taxPayable;

    /** Mã 300: Tổng nợ phải trả = 310 + 320 */
    @Column(name = "total_liabilities")
    private Double totalLiabilities;

    /** Mã 410: Vốn đầu tư của chủ sở hữu (tổng initialBalance của Fund) */
    @Column(name = "owner_equity")
    private Double ownerEquity;

    /** Mã 400: Vốn chủ sở hữu = 410 */
    @Column(name = "total_equity")
    private Double totalEquity;

    /** Mã 500: Tổng nguồn vốn = 300 + 400 */
    @Column(name = "total_liabilities_and_equity")
    private Double totalLiabilitiesAndEquity;
}