package vn.edu.hcmuaf.fit.quanlythuchi.service.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ReportRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.UserRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final TransactionService transactionService;

    // ──────────────────────────────────────────────────────────────
    //  TẠO BÁO CÁO MỚI
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ReportResponseDTO createReport(ReportDTO request) {
        validateRequest(request);

        // Lấy người tạo
        User createdBy = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy người dùng với ID: " + request.getCreatedBy()));

        // Tự động tính số liệu từ transactions
        Double totalIncome  = safeDouble(reportRepository.sumIncomeByDateRange(request.getFromDate(), request.getToDate()));
        Double totalExpense = safeDouble(reportRepository.sumExpenseByDateRange(request.getFromDate(), request.getToDate()));
        Double netBalance   = totalIncome - totalExpense;

        Report report = new Report();
        report.setTitle(request.getTitle());
        report.setType(request.getType().toUpperCase());
        report.setFromDate(request.getFromDate());
        report.setToDate(request.getToDate());
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.setNetBalance(netBalance);
        report.setNote(request.getNote());
        report.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT");
        report.setCreatedBy(createdBy);
        report.setCreatedAt(new Date());
        report.setUpdatedAt(new Date());
        report.setIsDeleted(false);

        // Tính và lưu chỉ tiêu bảng cân đối kế toán vào DB
        applyBalanceSheet(report);

        return toResponseDTO(reportRepository.save(report), false);
    }

    // ──────────────────────────────────────────────────────────────
    //  CẬP NHẬT BÁO CÁO
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ReportResponseDTO updateReport(Long id, ReportDTO request) {
        Report report = findActiveReport(id);

        if (report.getStatus() != null && "PUBLISHED".equalsIgnoreCase(report.getStatus())) {
            throw new RuntimeException("Không thể chỉnh sửa báo cáo đã PUBLISHED. Hãy đổi trạng thái về DRAFT trước.");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            report.setTitle(request.getTitle());
        }
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            report.setType(request.getType().toUpperCase());
        }
        if (request.getNote() != null) {
            report.setNote(request.getNote());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            report.setStatus(request.getStatus().toUpperCase());
        }

        // Nếu khoảng thời gian thay đổi → tái tính toàn bộ số liệu
        boolean dateChanged = false;
        if (request.getFromDate() != null) {
            report.setFromDate(request.getFromDate());
            dateChanged = true;
        }
        if (request.getToDate() != null) {
            report.setToDate(request.getToDate());
            dateChanged = true;
        }

        if (dateChanged) {
            Double totalIncome  = safeDouble(reportRepository.sumIncomeByDateRange(report.getFromDate(), report.getToDate()));
            Double totalExpense = safeDouble(reportRepository.sumExpenseByDateRange(report.getFromDate(), report.getToDate()));
            report.setTotalIncome(totalIncome);
            report.setTotalExpense(totalExpense);
            report.setNetBalance(totalIncome - totalExpense);
            // Tái tính cả chỉ tiêu bảng cân đối kế toán
            applyBalanceSheet(report);
        }

        report.setUpdatedAt(new Date());
        return toResponseDTO(reportRepository.save(report), false);
    }

    // ──────────────────────────────────────────────────────────────
    //  XÓA MỀM BÁO CÁO
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteReport(Long id) {
        Report report = findActiveReport(id);
        report.setIsDeleted(true);
        report.setUpdatedAt(new Date());
        reportRepository.save(report);
    }

    // ──────────────────────────────────────────────────────────────
    //  LẤY CHI TIẾT
    // ──────────────────────────────────────────────────────────────
    @Override
    public ReportResponseDTO getReportById(Long id) {
        return toResponseDTO(findActiveReport(id), true);
    }

    // ──────────────────────────────────────────────────────────────
    //  LẤY DANH SÁCH
    // ──────────────────────────────────────────────────────────────
    @Override
    public List<ReportResponseDTO> getAllReports() {
        return reportRepository.findByIsDeletedFalse()
                .stream().map(report -> toResponseDTO(report, false)).collect(Collectors.toList());
    }

    @Override
    public List<ReportResponseDTO> getReportsByType(String type) {
        return reportRepository.findByTypeAndIsDeletedFalse(type.toUpperCase())
                .stream().map(report -> toResponseDTO(report, false)).collect(Collectors.toList());
    }

    @Override
    public List<ReportResponseDTO> getReportsByUser(Long userId) {
        return reportRepository.findByCreatedBy_IdAndIsDeletedFalse(userId)
                .stream().map(r -> toResponseDTO(r, false)).collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────
    //  TÁI TÍNH SỐ LIỆU
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ReportResponseDTO recalculate(Long id) {
        Report report = findActiveReport(id);

        // Tái tính thu/chi
        Double totalIncome  = safeDouble(reportRepository.sumIncomeByDateRange(report.getFromDate(), report.getToDate()));
        Double totalExpense = safeDouble(reportRepository.sumExpenseByDateRange(report.getFromDate(), report.getToDate()));
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.setNetBalance(totalIncome - totalExpense);

        // Tái tính toàn bộ chỉ tiêu bảng cân đối và lưu vào DB
        applyBalanceSheet(report);
        report.setUpdatedAt(new Date());

        return toResponseDTO(reportRepository.save(report), true);
    }

    // ──────────────────────────────────────────────────────────────
    //  HELPER
    // ──────────────────────────────────────────────────────────────

    /**
     * Tính toán các chỉ tiêu bảng cân đối kế toán (mã 110–500)
     * và ghi thẳng vào các field của Report entity để chuẩn bị save xuống DB.
     * Phương thức này dùng chung cho createReport(), updateReport(), recalculate().
     */
    private void applyBalanceSheet(Report report) {
        Double cash           = safeDouble(fundRepository.getTotalFundBalance().orElse(0.0));
        Double initialCapital = safeDouble(reportRepository.getTotalInitialCapital());
        Double receivable     = 0.0;
        Double payable        = 0.0;
        Double taxExp         = 0.0;

        if (report.getFromDate() != null && report.getToDate() != null) {
            receivable = safeDouble(reportRepository.sumReceivableByDateRange(report.getFromDate(), report.getToDate()));
            payable    = safeDouble(reportRepository.sumPayableByDateRange(report.getFromDate(), report.getToDate()));
            taxExp     = safeDouble(reportRepository.sumTaxExpenseByDateRange(report.getFromDate(), report.getToDate()));
        }

        Double totalAssets      = cash + receivable;
        Double totalLiabilities = payable + taxExp;
        Double totalEquity      = initialCapital;
        Double totalLiabAndEq   = totalLiabilities + totalEquity;

        report.setCashAndEquivalents(cash);
        report.setAccountsReceivable(receivable);
        report.setTotalAssets(totalAssets);
        report.setAccountsPayable(payable);
        report.setTaxPayable(taxExp);
        report.setTotalLiabilities(totalLiabilities);
        report.setOwnerEquity(initialCapital);
        report.setTotalEquity(totalEquity);
        report.setTotalLiabilitiesAndEquity(totalLiabAndEq);
    }

    private Report findActiveReport(Long id) {
        return reportRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy báo cáo với ID: " + id + " hoặc báo cáo đã bị xóa."));
    }

    private void validateRequest(ReportDTO request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề báo cáo không được để trống!");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại báo cáo không được để trống! (MONTHLY / QUARTERLY / YEARLY / CUSTOM)");
        }
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống!");
        }
        if (request.getFromDate().after(request.getToDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc!");
        }
        if (request.getCreatedBy() == null) {
            throw new IllegalArgumentException("Cần cung cấp ID người tạo báo cáo!");
        }
    }

    private Double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    /**
     * Chuyển Report entity → ReportResponseDTO.
     * Các chỉ tiêu bảng cân đối được đọc thẳng từ entity (đã được lưu vào DB),
     * không còn tính toán động tại đây nữa.
     */
    private ReportResponseDTO toResponseDTO(Report report, boolean isIncludeDetails) {
        ReportResponseDTO dto = ReportResponseDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .type(report.getType())
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .totalIncome(report.getTotalIncome())
                .totalExpense(report.getTotalExpense())
                .netBalance(report.getNetBalance())
                .note(report.getNote())
                .status(report.getStatus())
                .createdBy(report.getCreatedBy() != null ? report.getCreatedBy().getId() : null)
                .createdByName(report.getCreatedBy() != null ? report.getCreatedBy().getFullName() : "")
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                // Đọc từ DB – không tính lại mỗi lần GET
                .cashAndEquivalents(report.getCashAndEquivalents())
                .accountsReceivable(report.getAccountsReceivable())
                .totalAssets(report.getTotalAssets())
                .accountsPayable(report.getAccountsPayable())
                .taxPayable(report.getTaxPayable())
                .totalLiabilities(report.getTotalLiabilities())
                .ownerEquity(report.getOwnerEquity())
                .totalEquity(report.getTotalEquity())
                .totalLiabilitiesAndEquity(report.getTotalLiabilitiesAndEquity())
                .build();

        if (isIncludeDetails && report.getFromDate() != null && report.getToDate() != null) {
            List<Transaction> txEntities =
                    reportRepository.findTransactionsByDateRange(report.getFromDate(), report.getToDate());

            List<TransactionDTO> txDTOs = txEntities.stream()
                    .map(transactionService::toDTO)
                    .collect(Collectors.toList());

            dto.setTransactions(txDTOs);
            dto.setTransactionCount(txDTOs.size());
        }

        return dto;
    }
}