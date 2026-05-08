package vn.edu.hcmuaf.fit.quanlythuchi.service.warning;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;

@Service
public interface SpendingWarningService {

    /**
     * Phân tích chi tiêu theo thuật toán Moving Average & Threshold.
     * Gọi sau khi tạo/cập nhật giao dịch EXPENSE.
     *
     * @return SpendingWarningDTO chứa thông tin cảnh báo (hoặc không cảnh báo)
     */
    SpendingWarningDTO analyze(Long categoryId, TransactionDTO requestDTO);
}