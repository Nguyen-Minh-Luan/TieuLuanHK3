package vn.edu.hcmuaf.fit.quanlythuchi.service.notification;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;

public interface RiskNotificationService {
    void checkAndNotifySpendingWarning(SpendingWarningDTO warning);
}