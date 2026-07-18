package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "funds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String type;
    @Column(nullable = false)
    private Double initialBalance;
    private Double currentBalance;
    private String status;
    private Date created_at;
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(length = 50)
    private String code;

    @Column(length = 1000)
    private String note;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "account_code", length = 10)
    private String accountCode;
}
