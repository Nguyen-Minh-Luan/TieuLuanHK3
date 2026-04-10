package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

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
}
