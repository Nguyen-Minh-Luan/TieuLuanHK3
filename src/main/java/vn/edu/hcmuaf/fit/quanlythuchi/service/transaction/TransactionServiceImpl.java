package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.*;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.*;
import vn.edu.hcmuaf.fit.quanlythuchi.service.debt.DebtService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation.FundReconciliationService;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final FundRepository fundRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final DebtRepository debtRepository;
    private final DebtService debtService;
    private final FundReconciliationService reconciliationService;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionDTO request) {
        // 1. Kiểm tra ngoại lệ đầu vào cơ bản
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0");
        }
        if (!"INCOME".equalsIgnoreCase(request.getType()) && !"EXPENSE".equalsIgnoreCase(request.getType())) {
            throw new IllegalArgumentException("Loại giao dịch không hợp lệ (Chỉ nhận INCOME hoặc EXPENSE)");
        }

        // 2. Lấy Fund và kiểm tra ngoại lệ nếu không tồn tại
        Fund fund = fundRepository.findById(request.getFundId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy Nguồn tiền (Fund) với ID: " + request.getFundId()));
        
        Date txDate = request.getTransactionDate() != null ? request.getTransactionDate() : new Date();
        reconciliationService.assertNotLocked(request.getFundId(), txDate);

        // 3. Xử lý logic cộng trừ số dư (Tuần tự, không đệ quy)
        Double currentBalance = fund.getCurrentBalance() != null ? fund.getCurrentBalance() : 0.0;

        if ("INCOME".equalsIgnoreCase(request.getType())) {
            fund.setCurrentBalance(currentBalance + request.getAmount());
        } else {
            if (currentBalance < request.getAmount()) {
                throw new RuntimeException("Số dư trong nguồn tiền không đủ để thực hiện Phiếu Chi!");
            }
            fund.setCurrentBalance(currentBalance - request.getAmount());
        }
        // Cập nhật số dư Fund
        fundRepository.save(fund);
        // 4. Khởi tạo và Map DTO sang Entity
        Transaction transaction = new Transaction();
        // --- BẮT ĐẦU GẮN CÁC ENTITY LIÊN QUAN ---
        // Gắn Fund
        transaction.setFund(fund);
        // Lấy và gắn Category (Bắt buộc)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy Hạng mục (Category) với ID: " + request.getCategoryId()));
        transaction.setCategories(category);
        // Lấy và gắn User (Bắt buộc - Người thực hiện giao dịch)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy Người dùng (User) với ID: " + request.getUserId()));
        transaction.setUser(user);

        // Lấy và gắn Partner (KHÔNG bắt buộc - Chỉ query khi client có gửi lên
        // partnerId)
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy Đối tác (Partner) với ID: " + request.getPartnerId()));
            transaction.setPartner(partner);
        }

        // --- KẾT THÚC GẮN ENTITY ---

        // Gắn các thuộc tính cơ bản
        transaction.setType(request.getType().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setAccompaniedBy(request.getAccompaniedBy());
        transaction.setOriginalDocuments(request.getOriginalDocuments());

        // Tạo mã giao dịch ngẫu nhiên
        transaction.setTransaction_code("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setTransaction_date(txDate);
        transaction.setCreated_at(new Date());
        transaction.setDatetime(new Date());
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setHasWarning(request.getHasWarning());
        transaction.setWarningLevel(request.getWarningLevel() != null ? request.getWarningLevel() : "NORMAL");

        // ── TÍCH HỢP NỢ: Kiểm tra và cập nhật khoản nợ (nếu phiếu này đang thanh toán
        // cho một khoản nợ) ──
        if (request.getDebtId() != null) {
            Debt debt = debtRepository.findByIdAndIsDeletedFalse(request.getDebtId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy khoản nợ với ID: " + request.getDebtId()));

            // Validation: Chiều giao dịch phải khớp với loại nợ
            // - Nợ RECEIVABLE (khách nợ mình) → thu về khi khách trả → loại phải là INCOME
            // - Nợ PAYABLE (mình nợ) → chi trả khi trả nợ → loại phải là EXPENSE
            boolean isValidDirection = ("RECEIVABLE".equals(debt.getDebtType())
                    && "INCOME".equalsIgnoreCase(request.getType())) ||
                    ("PAYABLE".equals(debt.getDebtType()) && "EXPENSE".equalsIgnoreCase(request.getType()));

            if (!isValidDirection) {
                throw new RuntimeException(
                        "Loại giao dịch không khớp với loại nợ! " +
                                "Nợ RECEIVABLE phải dùng phiếu THU (INCOME). " +
                                "Nợ PAYABLE phải dùng phiếu CHI (EXPENSE).");
            }

            // Gắn FK vào transaction để lưu vào DB (phải làm TRƯỚC khi save)
            transaction.setDebt(debt);
        }

        // Lưu giao dịch vào DB (đã có debt_id nếu có)
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Cập nhật paidAmount trên Debt — gọi sau save để transaction đã tồn tại trong
        // DB
        if (request.getDebtId() != null) {
            debtService.applyPayment(request.getDebtId(), request.getAmount());
        }

        return toDTO(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(Long oldId, TransactionDTO newRequest) {
        // Lấy giao dịch cũ lên
        Transaction oldTx = transactionRepository.findById(oldId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // Kiểm tra khóa sổ với giao dịch cũ
        reconciliationService.assertNotLocked(oldTx.getFund().getId(), oldTx.getTransaction_date());
        // Kiểm tra khóa sổ với dữ liệu mới (nếu có đổi ngày hoặc đổi quỹ)
        Long targetFundId = newRequest.getFundId() != null ? newRequest.getFundId() : oldTx.getFund().getId();
        Date targetTxDate = newRequest.getTransactionDate() != null ? newRequest.getTransactionDate() : oldTx.getTransaction_date();
        reconciliationService.assertNotLocked(targetFundId, targetTxDate);

        // Không cho phép cập nhật phiếu đã liên kết với khoản nợ
        if (oldTx.getDebt() != null) {
            throw new RuntimeException(
                    "Không thể cập nhật phiếu đã liên kết với khoản nợ (Debt ID: " +
                            oldTx.getDebt().getId() + "). Hãy hủy phiếu và tạo phiếu mới.");
        }

        // 1. HOÀN TÁC SỐ DƯ CỦA GIAO DỊCH CŨ (Trên Fund cũ)
        Fund oldFund = oldTx.getFund();
        if ("INCOME".equalsIgnoreCase(oldTx.getType())) {
            oldFund.setCurrentBalance(oldFund.getCurrentBalance() - oldTx.getAmount());
        } else {
            oldFund.setCurrentBalance(oldFund.getCurrentBalance() + oldTx.getAmount());
        }

        // 2. ĐÁNH DẤU BẢN GHI CŨ LÀ UPDATED
        oldTx.setStatus(TransactionStatus.UPDATED);
        transactionRepository.save(oldTx);

        // 3. TẠO BẢN GHI MỚI VÀ MAP DỮ LIỆU
        Transaction newTx = new Transaction();
        newTx.setParentId(oldId);
        newTx.setStatus(TransactionStatus.ACTIVE);

        // --- Bắt đầu set các trường từ Request ---

        // Lấy và gắn Nguồn tiền (Có thể là Fund cũ, hoặc Fund mới nếu người dùng chọn
        // đổi Nguồn tiền)
        Fund targetFund = oldFund;
        // Kiểm tra: Nếu client có gửi fundId lên VÀ fundId đó khác với quỹ cũ thì mới
        // query DB
        if (newRequest.getFundId() != null && !oldFund.getId().equals(newRequest.getFundId())) {
            targetFund = fundRepository.findById(newRequest.getFundId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy Nguồn tiền (Fund) mới với ID: " + newRequest.getFundId()));
        }
        newTx.setFund(targetFund);

        // 2. Lấy và gắn Category
        Category category = oldTx.getCategories(); // Mặc định giữ lại hạng mục cũ
        if (newRequest.getCategoryId() != null
                && (category == null || !category.getId().equals(newRequest.getCategoryId()))) {
            category = categoryRepository.findById(newRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy Hạng mục với ID: " + newRequest.getCategoryId()));
        }
        newTx.setCategories(category);

        // 3. Lấy và gắn User
        User user = oldTx.getUser(); // Mặc định giữ lại user cũ
        if (newRequest.getUserId() != null && (user == null || !user.getId().equals(newRequest.getUserId()))) {
            user = userRepository.findById(newRequest.getUserId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy Người dùng với ID: " + newRequest.getUserId()));
        }
        newTx.setUser(user);

        // 4. Lấy và gắn Partner
        Partner partner = oldTx.getPartner(); // Mặc định giữ lại partner cũ
        if (newRequest.getPartnerId() != null
                && (partner == null || !partner.getId().equals(newRequest.getPartnerId()))) {
            partner = partnerRepository.findById(newRequest.getPartnerId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy Đối tác với ID: " + newRequest.getPartnerId()));
        }
        newTx.setPartner(partner);

        // 5. Map các trường cơ bản (Nếu client không gửi thì lấy lại giá trị cũ)
        newTx.setType(newRequest.getType() != null ? newRequest.getType().toUpperCase() : oldTx.getType());
        newTx.setAmount(newRequest.getAmount() != null ? newRequest.getAmount() : oldTx.getAmount());
        newTx.setNote(newRequest.getNote() != null ? newRequest.getNote() : oldTx.getNote());
        newTx.setAccompaniedBy(
                newRequest.getAccompaniedBy() != null ? newRequest.getAccompaniedBy() : oldTx.getAccompaniedBy());
        newTx.setOriginalDocuments(newRequest.getOriginalDocuments() != null ? newRequest.getOriginalDocuments()
                : oldTx.getOriginalDocuments());

        // Xử lý các trường thời gian và mã giao dịch
        newTx.setTransaction_code("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newTx.setTransaction_date(targetTxDate); // Cho phép cập nhật ngày giao dịch nếu client gửi
        newTx.setCreated_at(new Date());
        newTx.setDatetime(new Date());
        newTx.setHasWarning(newRequest.getHasWarning() != null ? newRequest.getHasWarning() : oldTx.getHasWarning());
        newTx.setWarningLevel(newRequest.getWarningLevel() != null ? newRequest.getWarningLevel() : oldTx.getWarningLevel());

        // --- Kết thúc set các trường ---

        // 4. CẬP NHẬT SỐ DƯ THEO SỐ TIỀN MỚI (Trên targetFund)
        if ("INCOME".equalsIgnoreCase(newTx.getType())) {
            targetFund.setCurrentBalance(targetFund.getCurrentBalance() + newTx.getAmount());
        } else {
            // Validation: Nếu là phiếu chi, quỹ mới phải đủ tiền
            if (targetFund.getCurrentBalance() < newTx.getAmount()) {
                throw new RuntimeException("Số dư trong nguồn tiền không đủ để thực hiện Phiếu Chi cập nhật!");
            }
            targetFund.setCurrentBalance(targetFund.getCurrentBalance() - newTx.getAmount());
        }
        // 5. LƯU THAY ĐỔI VÀO DATABASE
        // Nếu đổi sang quỹ khác, phải lưu quỹ cũ (đã hoàn tiền) riêng
        if (!oldFund.getId().equals(targetFund.getId())) {
            fundRepository.save(oldFund);
        }
        fundRepository.save(targetFund); // Lưu quỹ mới (đã trừ/cộng tiền mới)

        return toDTO(transactionRepository.save(newTx));
    }

    @Override
    @Transactional
    public void cancelTransaction(Long txId) {
        // 1. Lấy giao dịch an toàn
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với ID: " + txId));
                
        // Kiểm tra khóa sổ
        reconciliationService.assertNotLocked(tx.getFund().getId(), tx.getTransaction_date());

        // 2. CHECK LOGIC: Chỉ cho phép hủy nếu giao dịch đang ở trạng thái ACTIVE
        if (TransactionStatus.ACTIVE != tx.getStatus()) {
            throw new IllegalStateException("Hủy thất bại: Chỉ có thể hủy giao dịch đang ở trạng thái ACTIVE!");
        }

        // 3. Đổi trạng thái thành CANCELLED
        tx.setStatus(TransactionStatus.CANCELLED);

        // 4. Hoàn lại tiền vào quỹ
        Fund fund = tx.getFund();
        if ("INCOME".equalsIgnoreCase(tx.getType())) {
            fund.setCurrentBalance(fund.getCurrentBalance() - tx.getAmount());
        } else {
            fund.setCurrentBalance(fund.getCurrentBalance() + tx.getAmount());
        }

        // 5. Hoàn tác paidAmount trên Debt (nếu phiếu này có liên kết khoản nợ)
        if (tx.getDebt() != null) {
            Debt debt = tx.getDebt();
            double newPaid = (debt.getPaidAmount() != null ? debt.getPaidAmount() : 0.0) - tx.getAmount();
            debt.setPaidAmount(Math.max(newPaid, 0.0));
            // Nếu đã đánh dấu isPaid thì reset lại
            debt.setIsPaid(false);
            debt.setPaymentDate(null);
            debt.setUpdatedAt(new Date());
            debtRepository.save(debt);
        }

        // 6. Lưu thay đổi
        fundRepository.save(fund);
        transactionRepository.save(tx);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        return toDTO(transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + id)));
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getAllTransactions(String keyword, String type, String status,
                                                   Long fundId, Long categoryId, Long partnerId,
                                                   Long userId, Date fromDate, Date toDate,
                                                   int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        TransactionStatus enumStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                enumStatus = TransactionStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore if invalid
            }
        }
        return transactionRepository
                .searchTransactions(keyword, type, enumStatus, fundId, categoryId, partnerId,
                                    userId, fromDate, toDate, pageable)
                .map(this::toDTO);
    }

    @Override
    public Double getTotalIncome() {
        try {
            // Gọi hàm tính tổng từ Repository và truyền loại là INCOME (Thu)
            Double totalIncome = transactionRepository.getTotalIncome();

            // Đảm bảo an toàn tuyệt đối (đề phòng database trả về null)
            if (totalIncome == null) {
                return 0.0;
            }
            return totalIncome;

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tính tổng thu: " + e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi tính tổng thu. Vui lòng thử lại sau!");
        }
    }

    @Override
    public Double getTotalExpense() {
        try {
            // Gọi hàm tính tổng từ Repository và truyền loại là EXPENSE (Chi)
            Double totalExpense = transactionRepository.getTotalExpense();

            // Đảm bảo an toàn tuyệt đối
            if (totalExpense == null) {
                return 0.0;
            }
            return totalExpense;

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tính tổng chi: " + e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi tính tổng chi. Vui lòng thử lại sau!");
        }
    }

    public TransactionDTO toDTO(Transaction tx) {
        return TransactionDTO.builder()
                .id(tx.getId())
                .parentId(tx.getParentId() != null ? tx.getParentId() : null)
                .fundId(tx.getFund() != null ? tx.getFund().getId() : null)
                .categoryId(tx.getCategories() != null ? tx.getCategories().getId() : null)
                .userId(tx.getUser() != null ? tx.getUser().getId() : null)
                .partnerId(tx.getPartner() != null ? tx.getPartner().getId() : null)
                .type(tx.getType())
                .status(tx.getStatus() != null ? tx.getStatus().name() : null)
                .amount(tx.getAmount())
                .note(tx.getNote())
                .userName(tx.getUser() != null ? tx.getUser().getFullName() : null)
                .transactionCode(tx.getTransaction_code())
                .transactionDate(tx.getTransaction_date())
                .createdAt(tx.getCreated_at())
                .hasWarning(tx.getHasWarning())
                .warningLevel(tx.getWarningLevel() != null ? tx.getWarningLevel() : "NORMAL")
                .accompaniedBy(tx.getAccompaniedBy())
                .originalDocuments(tx.getOriginalDocuments())
                .debtId(tx.getDebt() != null ? tx.getDebt().getId() : null)
                .build();
    }
}