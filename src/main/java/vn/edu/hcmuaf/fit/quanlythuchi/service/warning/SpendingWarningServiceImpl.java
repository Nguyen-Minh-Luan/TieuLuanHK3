package vn.edu.hcmuaf.fit.quanlythuchi.service.warning;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Category;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.CategoryRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpendingWarningServiceImpl implements SpendingWarningService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    // -------------------------------------------------------
    //  CẤU HÌNH NGƯỠNG (Có thể đưa ra application.properties)
    // -------------------------------------------------------
    /** Số tháng lịch sử dùng để tính trung bình động */
    private static final int HISTORY_MONTHS = 3;

    /** Vượt >= 20%  → WARNING (cảnh báo nhẹ) */
    private static final double WARNING_THRESHOLD = 0.20;

    /** Vượt >= 50%  → CRITICAL (cảnh báo đỏ) */
    private static final double CRITICAL_THRESHOLD = 0.50;


    @Override
    public SpendingWarningDTO analyze(Long categoryId, TransactionDTO requestDTO) {
        Double newAmount = requestDTO.getAmount();
        // ── 1. Lấy tên hạng mục ──────────────────────────────────────────────
        String categoryName = categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Hạng mục #" + categoryId);

        // ── 2. Lấy lịch sử chi tiêu theo tháng (N tháng trước, không tính tháng hiện tại) ──
        List<Object[]> history = transactionRepository
                .getMonthlyTotalByCategoryForLastNMonths(categoryId, HISTORY_MONTHS);

        // Không đủ dữ liệu lịch sử → bỏ qua phân tích
        if (history == null || history.isEmpty()) {
            return SpendingWarningDTO.builder()
                    .hasWarning(false)
                    .categoryName(categoryName)
                    .level("NORMAL")
                    .message("Chưa đủ dữ liệu lịch sử để phân tích xu hướng chi tiêu.")
                    .build();
        }

        // ── 3. Tính Moving Average từ lịch sử ────────────────────────────────
        double sumHistory = 0.0;
        for (Object[] row : history) {
            // row = [year, month, total]
            double monthTotal = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            sumHistory += monthTotal;
        }
        double historicalAverage = sumHistory / history.size();

        // Nếu trung bình = 0 (chưa từng chi hạng mục này), không có cơ sở so sánh
        if (historicalAverage <= 0) {
            return SpendingWarningDTO.builder()
                    .hasWarning(false)
                    .categoryName(categoryName)
                    .level("NORMAL")
                    .message("Hạng mục này chưa có lịch sử chi tiêu để so sánh.")
                    .build();
        }

        // ── 4. Lấy tổng chi tháng hiện tại (đã bao gồm giao dịch vừa lưu) ──
        //      newAmount đã được lưu vào DB trước khi hàm này được gọi
        Double currentMonthFromDB = transactionRepository.getCurrentMonthTotalByCategory(categoryId);
        if (currentMonthFromDB == null) currentMonthFromDB = 0.0;
        Double currentMonthTotal = currentMonthFromDB + newAmount;
        // ── 5. Tính % vượt mức ───────────────────────────────────────────────
        double overagePercent = (currentMonthTotal - historicalAverage) / historicalAverage;

        // ── 6. Đánh giá ngưỡng & trả về kết quả ─────────────────────────────
        if (overagePercent >= CRITICAL_THRESHOLD) {
            return buildWarning(
                    categoryName, currentMonthTotal, historicalAverage, overagePercent,
                    "CRITICAL",
                    String.format(
                            "🔴 CẢNH BÁO NGHIÊM TRỌNG: Chi phí \"%s\" tháng này đang cao hơn %.0f%% " +
                                    "so với trung bình %d tháng trước (%.0f → %.0f). " +
                                    "Vui lòng kiểm tra lại ngân sách!",
                            categoryName,
                            overagePercent * 100,
                            history.size(),
                            historicalAverage,
                            currentMonthTotal));

        } else if (overagePercent >= WARNING_THRESHOLD) {
            return buildWarning(
                    categoryName, currentMonthTotal, historicalAverage, overagePercent,
                    "WARNING",
                    String.format(
                            "🟡 CẢNH BÁO: Chi phí \"%s\" tháng này đang cao hơn %.0f%% " +
                                    "so với trung bình %d tháng trước (%.0f → %.0f).",
                            categoryName,
                            overagePercent * 100,
                            history.size(),
                            historicalAverage,
                            currentMonthTotal));
        }

        // Trong ngưỡng bình thường
        return SpendingWarningDTO.builder()
                .hasWarning(false)
                .categoryName(categoryName)
                .currentMonthTotal(currentMonthTotal)
                .historicalAverage(historicalAverage)
                .overagePercent(overagePercent * 100)
                .level("NORMAL")
                .message(String.format(
                        "✅ Chi phí \"%s\" tháng này (%.0f) đang trong mức bình thường " +
                                "so với trung bình lịch sử (%.0f).",
                        categoryName, currentMonthTotal, historicalAverage))
                .build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private SpendingWarningDTO buildWarning(String categoryName, Double currentMonthTotal,
                                            Double historicalAverage, double overagePercent,
                                            String level, String message) {
        return SpendingWarningDTO.builder()
                .hasWarning(true)
                .categoryName(categoryName)
                .currentMonthTotal(currentMonthTotal)
                .historicalAverage(historicalAverage)
                .overagePercent(Math.round(overagePercent * 10000.0) / 100.0) // làm tròn 2 chữ số thập phân
                .level(level)
                .message(message)
                .build();
    }
}