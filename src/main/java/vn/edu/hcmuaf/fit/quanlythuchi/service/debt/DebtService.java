package vn.edu.hcmuaf.fit.quanlythuchi.service.debt;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.DebtRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.DebtResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Debt;

import java.util.List;
import java.util.Map;

public interface DebtService {

    /** Tạo khoản nợ mới */
    DebtResponse createDebt(DebtRequest request);

    /** Cập nhật thông tin khoản nợ (chỉ cho phép khi chưa isPaid) */
    DebtResponse updateDebt(Long id, DebtRequest request);

    /** Xóa mềm khoản nợ */
    void deleteDebt(Long id);

    /** Lấy chi tiết một khoản nợ (kèm payments) */
    DebtResponse getDebtById(Long id);

    /** Lấy tất cả khoản nợ (không phân trang — dùng nội bộ) */
    List<DebtResponse> getAllDebts();

    /** Lấy danh sách nợ có phân trang, tìm kiếm và lọc */
    Page<DebtResponse> getAllDebts(String keyword, String debtType, Boolean isPaid,
                                   int page, int size, String sortBy, String sortDir);

    /** Lấy khoản nợ theo loại: RECEIVABLE hoặc PAYABLE */
    List<DebtResponse> getDebtsByType(String debtType);

    /** Lấy khoản nợ theo đối tác */
    List<DebtResponse> getDebtsByPartner(Long partnerId);

    /** Lấy khoản nợ chưa thanh toán xong */
    List<DebtResponse> getUnpaidDebts();

    /** Lấy khoản nợ chưa thanh toán xong theo loại */
    List<DebtResponse> getUnpaidDebtsByType(String debtType);

    /**
     * Tổng hợp số liệu nợ cho dashboard:
     * { totalRemainingReceivable, totalRemainingPayable }
     */
    Map<String, Double> getDebtSummary();

    /**
     * Cập nhật paidAmount khi có Transaction thanh toán nợ.
     * Được gọi nội bộ từ TransactionServiceImpl.
     * Tự động set isPaid = true và paymentDate nếu paidAmount >= totalAmount.
     *
     * @param debtId ID khoản nợ cần cập nhật
     * @param amount Số tiền vừa thanh toán thêm
     * @return Debt entity đã cập nhật
     */
    Debt applyPayment(Long debtId, Double amount);
}
