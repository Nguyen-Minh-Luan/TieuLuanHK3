package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.NotificationLog;

import java.time.LocalDateTime;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByRiskTypeAndTargetIdAndSentAtAfter(String riskType, String targetId, LocalDateTime sentAt);
}
