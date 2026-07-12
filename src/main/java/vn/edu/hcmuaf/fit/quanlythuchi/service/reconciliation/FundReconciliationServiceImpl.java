package vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation.FundReconciliationRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundReconciliation;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.exception.LockedPeriodException;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundReconciliationRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundReconciliationServiceImpl implements FundReconciliationService {

    private final FundReconciliationRepository reconciliationRepository;
    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Page<FundReconciliation> search(Long fundId, String status, Date fromDate, Date toDate, Pageable pageable) {
        return reconciliationRepository.searchReconciliations(fundId, status, fromDate, toDate, pageable);
    }

    @Override
    public FundReconciliation getById(Long id) {
        return reconciliationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên kiểm kê"));
    }

    @Override
    @Transactional
    public List<FundReconciliation> createReconciliations(FundReconciliationRequest request, User currentUser) {
        if (request.getPeriodEnd().after(new Date())) {
            throw new RuntimeException("Không thể kiểm kê cho khoảng thời gian ở tương lai");
        }
        if (request.getPeriodStart().after(request.getPeriodEnd())) {
            throw new RuntimeException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        List<FundReconciliation> result = new ArrayList<>();
        String groupCode = "KK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        for (Long fundId : request.getFundIds()) {
            Fund fund = fundRepository.findById(fundId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ id = " + fundId));

            if (reconciliationRepository.existsOverlapClosedPeriod(fundId, request.getPeriodStart(), request.getPeriodEnd())) {
                throw new RuntimeException("Quỹ " + fund.getName() + " đã có kỳ kiểm kê được chốt trong khoảng thời gian này");
            }

            if (reconciliationRepository.existsOverlapDraftPeriod(fundId, request.getPeriodStart(), request.getPeriodEnd())) {
                throw new RuntimeException("Quỹ " + fund.getName() + " đang có phiên kiểm kê nháp (DRAFT) trong khoảng thời gian này. Vui lòng hoàn tất hoặc xóa phiên nháp trước.");
            }

            FundReconciliation recon = new FundReconciliation();
            recon.setFund(fund);
            recon.setGroupCode(groupCode);
            recon.setPeriodStart(request.getPeriodStart());
            recon.setPeriodEnd(request.getPeriodEnd());
            recon.setStatus("DRAFT");
            recon.setCreatedBy(currentUser);
            recon.setCreatedAt(LocalDateTime.now());

            // Tính openingBalanceSystem
            Double openingBalance = 0.0;
            Optional<FundReconciliation> latestClosed = reconciliationRepository.findLatestClosedByFundId(fundId);
            if (latestClosed.isPresent()) {
                // Nếu đã có kỳ kiểm kê trước đó, lấy closing_balance_system + phát sinh từ kỳ trước đến trước periodStart
                FundReconciliation last = latestClosed.get();
                Double balanceSinceLast = transactionRepository.sumNetAmountByFundAndPeriod(
                        fundId,
                        new Date(last.getPeriodEnd().getTime() + 86400000L), // next day
                        new Date(request.getPeriodStart().getTime() - 86400000L) // prev day
                );
                openingBalance = last.getClosingBalanceSystem() + (balanceSinceLast != null ? balanceSinceLast : 0.0);
            } else {
                // Chưa từng kiểm kê -> lấy initialBalance + toàn bộ phát sinh đến trước periodStart
                Double allPastTransactions = transactionRepository.sumNetAmountByFundAndPeriod(
                        fundId,
                        new Date(0), // from beginning
                        new Date(request.getPeriodStart().getTime() - 86400000L)
                );
                openingBalance = fund.getInitialBalance() + (allPastTransactions != null ? allPastTransactions : 0.0);
            }
            recon.setOpeningBalanceSystem(openingBalance);

            // Tính phát sinh trong kỳ
            Double netInPeriod = transactionRepository.sumNetAmountByFundAndPeriod(fundId, request.getPeriodStart(), request.getPeriodEnd());
            recon.setClosingBalanceSystem(openingBalance + (netInPeriod != null ? netInPeriod : 0.0));

            result.add(reconciliationRepository.save(recon));
        }

        return result;
    }

    @Override
    @Transactional
    public FundReconciliation updateDraft(Long id, Double actualBalance, String note) {
        FundReconciliation recon = getById(id);
        if (!"DRAFT".equals(recon.getStatus())) {
            throw new RuntimeException("Chỉ có thể cập nhật phiên kiểm kê ở trạng thái DRAFT");
        }
        recon.setActualBalance(actualBalance);
        if (actualBalance != null) {
            recon.setDifference(actualBalance - recon.getClosingBalanceSystem());
        }
        recon.setNote(note);
        return reconciliationRepository.save(recon);
    }

    @Override
    @Transactional
    public FundReconciliation closeReconciliation(Long id, User currentUser) {
        FundReconciliation recon = getById(id);
        if (!"DRAFT".equals(recon.getStatus())) {
            throw new RuntimeException("Chỉ có thể chốt phiên kiểm kê ở trạng thái DRAFT");
        }
        if (recon.getActualBalance() == null) {
            throw new RuntimeException("Vui lòng nhập số dư thực tế trước khi chốt");
        }
        if (recon.getDifference() != 0 && (recon.getNote() == null || recon.getNote().trim().isEmpty())) {
            throw new RuntimeException("Bắt buộc phải nhập ghi chú lý do khi có chênh lệch");
        }

        recon.setStatus("CLOSED");
        recon.setClosedBy(currentUser);
        recon.setClosedAt(LocalDateTime.now());
        return reconciliationRepository.save(recon);
    }

    @Override
    @Transactional
    public FundReconciliation reopenReconciliation(Long id, String reason, User currentUser) {
        FundReconciliation recon = getById(id);
        if (!"CLOSED".equals(recon.getStatus())) {
            throw new RuntimeException("Chỉ có thể mở khóa phiên kiểm kê ở trạng thái CLOSED");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Bắt buộc phải nhập lý do mở khóa");
        }

        recon.setStatus("REOPENED");
        recon.setReopenedBy(currentUser);
        recon.setReopenedAt(LocalDateTime.now());
        recon.setReopenReason(reason);
        return reconciliationRepository.save(recon);
    }

    @Override
    @Transactional
    public void deleteDraft(Long id) {
        FundReconciliation recon = getById(id);
        if (!"DRAFT".equals(recon.getStatus())) {
            throw new RuntimeException("Chỉ có thể xóa phiên kiểm kê nháp (DRAFT)");
        }
        reconciliationRepository.delete(recon);
    }

    @Override
    public void assertNotLocked(Long fundId, Date transactionDate) {
        List<FundReconciliation> lockedPeriods = reconciliationRepository.findLockedPeriodsForDate(fundId, transactionDate);
        if (!lockedPeriods.isEmpty()) {
            FundReconciliation locked = lockedPeriods.get(0);
            throw new LockedPeriodException("Không thể thực hiện: ngày hạch toán thuộc kỳ kiểm kê " + 
                    locked.getPeriodStart().toString() + " - " + locked.getPeriodEnd().toString() + 
                    " đã được chốt. Vui lòng liên hệ Thủ quỹ để mở khóa nếu cần chỉnh sửa.");
        }
    }

    @Override
    public boolean isLocked(Long fundId, Date transactionDate) {
        return reconciliationRepository.existsLockedPeriodForDate(fundId, transactionDate);
    }
}
