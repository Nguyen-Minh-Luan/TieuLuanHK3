package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

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
}