package vn.edu.hcmuaf.fit.quanlythuchi.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.NotificationLog;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.NotificationLogRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskNotificationServiceImpl implements RiskNotificationService {

    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${app.notification.enabled:false}")
    private boolean isEnabled;

    @Value("${app.notification.admin-emails:}")
    private String adminEmails;

    @Value("${app.notification.spending-warning.enabled:false}")
    private boolean isSpendingWarningEnabled;

    @Override
    public void checkAndNotifySpendingWarning(SpendingWarningDTO warning) {
        if (!isEnabled || !isSpendingWarningEnabled) return;
        if (warning == null || !warning.isHasWarning()) return;
        if (!"WARNING".equals(warning.getLevel()) && !"CRITICAL".equals(warning.getLevel())) return;

        String targetId = "category_" + warning.getCategoryName().replaceAll("\\s", "_");

        if (hasBeenNotifiedRecently("SPENDING", targetId, 24)) {
            return;
        }

        String subject = "[CẢNH BÁO CHI TIÊU] Vượt ngân sách danh mục: " + warning.getCategoryName();
        String body = String.format("<html><body><h3>Cảnh báo chi tiêu mức độ %s</h3>" +
                        "<p>Danh mục: <b>%s</b></p>" +
                        "<p>Đã chi: <b>%,.0f</b> (Trung bình: %,.0f)</p>" +
                        "<p>Vượt mức: <b>%.2f%%</b></p>" +
                        "<p>Chi tiết: %s</p></body></html>",
                warning.getLevel(), warning.getCategoryName(), warning.getCurrentMonthTotal(),
                warning.getHistoricalAverage(), warning.getOveragePercent(), warning.getMessage());

        sendEmails(subject, body);
        logNotification("SPENDING", targetId);
    }

    private void sendEmails(String subject, String body) {
        if (adminEmails == null || adminEmails.isEmpty()) return;
        List<String> emails = Arrays.asList(adminEmails.split(","));
        for (String email : emails) {
            emailService.send(email.trim(), subject, body);
        }
    }

    private boolean hasBeenNotifiedRecently(String riskType, String targetId, int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return notificationLogRepository.existsByRiskTypeAndTargetIdAndSentAtAfter(riskType, targetId, cutoffTime);
    }

    private void logNotification(String riskType, String targetId) {
        NotificationLog logEntry = NotificationLog.builder()
                .riskType(riskType)
                .targetId(targetId)
                .sentAt(LocalDateTime.now())
                .build();
        notificationLogRepository.save(logEntry);
    }
}