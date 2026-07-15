package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "risk_type", nullable = false)
    private String riskType; // SPENDING, DEBT, RECONCILIATION

    @Column(name = "target_id", nullable = false)
    private String targetId; // ID of the transaction, debt, or reconciliation

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
