package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "debt_date", nullable = false)
    private Date debtDate;

    /**
     * "RECEIVABLE" → Đối tác đang nợ mình (mình sẽ thu về)
     * "PAYABLE"    → Mình đang nợ đối tác (mình sẽ phải chi trả)
     */
    @Column(name = "debt_type", nullable = false)
    private String debtType;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "paid_amount")
    private Double paidAmount = 0.0;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    @Column(name = "payment_date")
    private Date paymentDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categories_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private String note;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
}
