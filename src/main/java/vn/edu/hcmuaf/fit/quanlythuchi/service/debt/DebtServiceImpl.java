package vn.edu.hcmuaf.fit.quanlythuchi.service.debt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.DebtDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.*;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final PartnerRepository partnerRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository; // Inject thẳng repo, tránh circular dependency

    // ──────────────────────────────────────────────────────────────
    //  TẠO KHOẢN NỢ MỚI
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public DebtDTO createDebt(DebtDTO request) {
        validateRequest(request);

        Debt debt = new Debt();
        debt.setDebtDate(request.getDebtDate() != null ? request.getDebtDate() : new Date());
        debt.setDebtType(request.getDebtType().toUpperCase());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setPaidAmount(0.0);    // Luôn bắt đầu từ 0
        debt.setIsPaid(false);      // Luôn chưa trả khi mới tạo
        debt.setPaymentDate(null);
        debt.setNote(request.getNote());
        debt.setIsDeleted(false);
        debt.setCreatedAt(new Date());
        debt.setUpdatedAt(new Date());

        // Gắn Partner (bắt buộc)
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đối tác với ID: " + request.getPartnerId()));
        debt.setPartner(partner);

        // Gắn Category (bắt buộc)
        Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy hạng mục với ID: " + request.getCategoryId()));
        debt.setCategory(category);

        // Gắn User (bắt buộc)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy người dùng với ID: " + request.getUserId()));
        debt.setUser(user);

        return toDTO(debtRepository.save(debt));
    }

    // ──────────────────────────────────────────────────────────────
    //  CẬP NHẬT KHOẢN NỢ
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public DebtDTO updateDebt(Long id, DebtDTO request) {
        Debt debt = findActiveDebt(id);

        // Không cho phép sửa khoản nợ đã thanh toán xong
        if (Boolean.TRUE.equals(debt.getIsPaid())) {
            throw new RuntimeException(
                    "Không thể chỉnh sửa khoản nợ đã thanh toán xong (ID: " + id + ")");
        }

        if (request.getNote() != null) {
            debt.setNote(request.getNote());
        }
        if (request.getDebtDate() != null) {
            debt.setDebtDate(request.getDebtDate());
        }
        if (request.getDebtType() != null && !request.getDebtType().trim().isEmpty()) {
            validateDebtType(request.getDebtType());
            debt.setDebtType(request.getDebtType().toUpperCase());
        }
        // Chỉ cho phép sửa totalAmount nếu chưa có thanh toán nào
        if (request.getTotalAmount() != null) {
            if (debt.getPaidAmount() != null && debt.getPaidAmount() > 0) {
                throw new RuntimeException(
                        "Không thể sửa tổng số tiền nợ khi đã có thanh toán một phần. " +
                        "Số tiền đã trả: " + debt.getPaidAmount());
            }
            if (request.getTotalAmount() <= 0) {
                throw new IllegalArgumentException("Tổng số tiền nợ phải lớn hơn 0");
            }
            debt.setTotalAmount(request.getTotalAmount());
        }
        // Cập nhật đối tác nếu cần
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy đối tác với ID: " + request.getPartnerId()));
            debt.setPartner(partner);
        }
        // Cập nhật hạng mục nếu cần
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy hạng mục với ID: " + request.getCategoryId()));
            debt.setCategory(category);
        }

        debt.setUpdatedAt(new Date());
        return toDTO(debtRepository.save(debt));
    }

    // ──────────────────────────────────────────────────────────────
    //  XÓA MỀM
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteDebt(Long id) {
        Debt debt = findActiveDebt(id);
        // Cảnh báo nếu xóa khoản nợ chưa trả xong (nhưng vẫn cho phép xóa)
        if (!Boolean.TRUE.equals(debt.getIsPaid())) {
            System.out.println("WARN: Đang xóa khoản nợ chưa thanh toán xong, ID: " + id);
        }
        debt.setIsDeleted(true);
        debt.setUpdatedAt(new Date());
        debtRepository.save(debt);
    }

    // ──────────────────────────────────────────────────────────────
    //  LẤY DỮ LIỆU
    // ──────────────────────────────────────────────────────────────
    @Override
    public DebtDTO getDebtById(Long id) {
        Debt debt = findActiveDebt(id);
        DebtDTO dto = toDTO(debt);

        // Populate lịch sử thanh toán — chỉ cho GET /debts/{id}
        List<TransactionDTO> payments = transactionRepository
                .findByDebt_IdAndStatus(id, "ACTIVE")
                .stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
        dto.setPayments(payments);

        return dto;
    }

    @Override
    public List<DebtDTO> getAllDebts() {
        return debtRepository.findByIsDeletedFalse()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DebtDTO> getDebtsByType(String debtType) {
        validateDebtType(debtType);
        return debtRepository.findByDebtTypeAndIsDeletedFalse(debtType.toUpperCase())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DebtDTO> getDebtsByPartner(Long partnerId) {
        return debtRepository.findByPartner_IdAndIsDeletedFalse(partnerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DebtDTO> getUnpaidDebts() {
        return debtRepository.findByIsPaidFalseAndIsDeletedFalse()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DebtDTO> getUnpaidDebtsByType(String debtType) {
        validateDebtType(debtType);
        return debtRepository.findByDebtTypeAndIsPaidFalseAndIsDeletedFalse(debtType.toUpperCase())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getDebtSummary() {
        Double totalReceivable = debtRepository.getTotalRemainingReceivable();
        Double totalPayable    = debtRepository.getTotalRemainingPayable();
        return Map.of(
                "totalRemainingReceivable", totalReceivable != null ? totalReceivable : 0.0,
                "totalRemainingPayable",    totalPayable    != null ? totalPayable    : 0.0
        );
    }

    // ──────────────────────────────────────────────────────────────
    //  APPLY PAYMENT — gọi từ TransactionServiceImpl
    // ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Debt applyPayment(Long debtId, Double amount) {
        Debt debt = findActiveDebt(debtId);

        if (Boolean.TRUE.equals(debt.getIsPaid())) {
            throw new RuntimeException(
                    "Khoản nợ ID " + debtId + " đã được thanh toán xong trước đó, " +
                    "không thể ghi nhận thêm thanh toán.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        // Cộng dồn số tiền đã trả
        double newPaidAmount = (debt.getPaidAmount() != null ? debt.getPaidAmount() : 0.0) + amount;
        debt.setPaidAmount(newPaidAmount);

        // Kiểm tra đã trả đủ chưa
        if (newPaidAmount >= debt.getTotalAmount()) {
            debt.setIsPaid(true);
            debt.setPaymentDate(new Date()); // Ghi nhận ngày thanh toán xong
            System.out.println("INFO: Khoản nợ ID " + debtId + " đã được thanh toán xong.");
        }

        debt.setUpdatedAt(new Date());
        return debtRepository.save(debt);
    }

    // ──────────────────────────────────────────────────────────────
    //  HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private Debt findActiveDebt(Long id) {
        return debtRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy khoản nợ với ID: " + id + " hoặc khoản nợ đã bị xóa."));
    }

    private void validateRequest(DebtDTO request) {
        if (request.getDebtType() == null || request.getDebtType().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Loại nợ không được để trống! (RECEIVABLE = khách nợ mình | PAYABLE = mình nợ)");
        }
        validateDebtType(request.getDebtType());
        if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Tổng số tiền nợ phải lớn hơn 0");
        }
        if (request.getPartnerId() == null) {
            throw new IllegalArgumentException("Cần cung cấp ID đối tác liên quan đến khoản nợ");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Cần cung cấp ID hạng mục cho khoản nợ");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Cần cung cấp ID người lập khoản nợ");
        }
    }

    private void validateDebtType(String debtType) {
        if (!"RECEIVABLE".equalsIgnoreCase(debtType) && !"PAYABLE".equalsIgnoreCase(debtType)) {
            throw new IllegalArgumentException(
                    "Loại nợ không hợp lệ: '" + debtType + "'. Chỉ chấp nhận RECEIVABLE hoặc PAYABLE.");
        }
    }

    /** Map Transaction entity → TransactionDTO (dùng để populate payments trong DebtDTO) */
    private TransactionDTO toTransactionDTO(Transaction tx) {
        return TransactionDTO.builder()
                .parentId(tx.getParentId())
                .fundId(tx.getFund() != null ? tx.getFund().getId() : null)
                .categoryId(tx.getCategories() != null ? tx.getCategories().getId() : null)
                .userId(tx.getUser() != null ? tx.getUser().getId() : null)
                .partnerId(tx.getPartner() != null ? tx.getPartner().getId() : null)
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .note(tx.getNote())
                .transactionCode(tx.getTransaction_code())
                .transactionDate(tx.getTransaction_date())
                .createdAt(tx.getCreated_at())
                .reason(tx.getReason())
                .debtId(tx.getDebt() != null ? tx.getDebt().getId() : null)
                .build();
    }

    private DebtDTO toDTO(Debt debt) {
        double remaining = (debt.getTotalAmount() != null ? debt.getTotalAmount() : 0.0)
                         - (debt.getPaidAmount()   != null ? debt.getPaidAmount()   : 0.0);
        return DebtDTO.builder()
                .id(debt.getId())
                .debtDate(debt.getDebtDate())
                .debtType(debt.getDebtType())
                .totalAmount(debt.getTotalAmount())
                .paidAmount(debt.getPaidAmount())
                .isPaid(debt.getIsPaid())
                .paymentDate(debt.getPaymentDate())
                .partnerId(debt.getPartner() != null ? debt.getPartner().getId() : null)
                .partnerName(debt.getPartner() != null ? debt.getPartner().getName() : null)
                .categoryId(debt.getCategory() != null ? debt.getCategory().getId() : null)
                .categoryName(debt.getCategory() != null ? debt.getCategory().getName() : null)
                .userId(debt.getUser() != null ? debt.getUser().getId() : null)
                .createdByName(debt.getUser() != null ? debt.getUser().getFullName() : null)
                .note(debt.getNote())
                .createdAt(debt.getCreatedAt())
                .updatedAt(debt.getUpdatedAt())
                .remainingAmount(remaining)
                .build();
    }
}
