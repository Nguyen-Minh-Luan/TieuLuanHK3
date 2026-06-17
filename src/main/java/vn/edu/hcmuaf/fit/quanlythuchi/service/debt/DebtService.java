package vn.edu.hcmuaf.fit.quanlythuchi.service.debt;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.DebtDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Debt;

import java.util.List;
import java.util.Map;

public interface DebtService {

    /** Tạo khoản nợ mới */
    DebtDTO createDebt(DebtDTO request);

    /** Cập nhật thông tin khoản nợ (chỉ cho phép khi chưa isPaid) */
    DebtDTO updateDebt(Long id, DebtDTO request);

    /** Xóa mềm khoản nợ */
    void deleteDebt(Long id);

    /** Lấy chi tiết một khoản nợ */
    DebtDTO getDebtById(Long id);

    /** Lấy tất cả khoản nợ */
    List<DebtDTO> getAllDebts();

    /** Lấy khoản nợ theo loại: RECEIVABLE hoặc PAYABLE */
    List<DebtDTO> getDebtsByType(String debtType);

    /** Lấy khoản nợ theo đối tác */
    List<DebtDTO> getDebtsByPartner(Long partnerId);

    /** Lấy khoản nợ chưa thanh toán xong */
    List<DebtDTO> getUnpaidDebts();

    /** Lấy khoản nợ chưa thanh toán xong theo loại */
    List<DebtDTO> getUnpaidDebtsByType(String debtType);

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
