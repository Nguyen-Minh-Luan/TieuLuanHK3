package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionWithWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.notification.RiskNotificationService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.warning.SpendingWarningService;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SpendingWarningService spendingWarningService;
    private final RiskNotificationService riskNotificationService;
    private final JwtUtil jwtUtil;

    // ── Helper: lấy thông tin user hiện tại từ JWT trong request ──
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserFromJwtToken(token);
    }

    /**
     * Kiểm tra quyền sở hữu phiếu.
     * - ROLE_ADMIN (role=1): toàn quyền, luôn trả về true.
     * - ROLE_KETOAN (role=2): chỉ được thao tác phiếu do chính mình tạo.
     * - Các role khác (THUQUY=3, TONGHOP=4): không có quyền ghi transaction nên
     *   không cần ownership check ở đây (đã chặn ở @PreAuthorize).
     */
    private boolean isOwnerOrAdmin(User currentUser, TransactionDTO transaction) {
        if (currentUser.getRole() == null) return false;
        if (currentUser.getRole() == 1) return true; // Admin: toàn quyền
        if (currentUser.getRole() == 2) {
            // Kế toán Thu Chi: chỉ phiếu do mình tạo
            return currentUser.getId() != null
                    && currentUser.getId().equals(transaction.getUserId());
        }
        return false;
    }

    // --- 1. TẠO MỚI GIAO DỊCH (tích hợp cảnh báo chi tiêu thông minh) ---
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<TransactionWithWarningDTO>> createTransaction(
            @RequestBody TransactionDTO requestDTO,
            HttpServletRequest request) {

        // Bước 0: Luôn lấy userId từ JWT — không tin dữ liệu client gửi lên
        User currentUser = getCurrentUser(request);
        requestDTO.setUserId(currentUser.getId());

        // Bước 1: Phân tích cảnh báo TRƯỚC KHI lưu
        SpendingWarningDTO warning = null;
        if ("EXPENSE".equalsIgnoreCase(requestDTO.getType())
                && requestDTO.getCategoryId() != null) {
            warning = spendingWarningService.analyze(requestDTO.getCategoryId(), requestDTO);

            // Bước 2: Nếu có cảnh báo → đánh dấu isOverBudget trước khi lưu
            if (warning != null && warning.isHasWarning()) {
                requestDTO.setHasWarning(true);
                requestDTO.setWarningLevel(warning.getLevel());
            } else {
                requestDTO.setHasWarning(false);
                requestDTO.setWarningLevel(warning != null ? warning.getLevel() : "NORMAL");
            }
        } else {
            requestDTO.setHasWarning(false);
            requestDTO.setWarningLevel("NORMAL");
        }

        // Bước 1: Lưu giao dịch vào DB (logic cũ giữ nguyên)
        TransactionDTO savedTransaction = transactionService.createTransaction(requestDTO);
        
        // Notify admin via email if there is a warning (Real-time trigger)
        if (warning != null && warning.isHasWarning()) {
            riskNotificationService.checkAndNotifySpendingWarning(warning);
        }
        
        // Bước 3: Đóng gói kết quả giao dịch + cảnh báo vào 1 response duy nhất
        TransactionWithWarningDTO result = TransactionWithWarningDTO.builder()
                .transaction(savedTransaction)
                .warning(warning)
                .build();

        // Tùy chỉnh message response dựa trên mức độ cảnh báo
        String responseMessage = "Tạo giao dịch thành công";
        if (warning != null && warning.isHasWarning()) {
            responseMessage = "CRITICAL".equals(warning.getLevel())
                    ? "Tạo giao dịch thành công - ⚠️ Phát hiện chi tiêu bất thường nghiêm trọng!"
                    : "Tạo giao dịch thành công - ⚠️ Phát hiện chi tiêu vượt mức bình thường!";
        }

        return ApiResponse.created(result, responseMessage);
    }

    // --- 1.1 TẠO MỚI GIAO DỊCH VÀ UPLOAD CHỨNG TỪ ---
    @PostMapping(value = "/with-documents", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<TransactionWithWarningDTO>> createTransactionWithDocuments(
            @RequestPart("transaction") TransactionDTO requestDTO,
            @RequestPart(value = "files", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> files,
            @RequestPart(value = "descriptions", required = false) java.util.List<String> descriptions,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);
        requestDTO.setUserId(currentUser.getId());

        SpendingWarningDTO warning = null;
        if ("EXPENSE".equalsIgnoreCase(requestDTO.getType())
                && requestDTO.getCategoryId() != null) {
            warning = spendingWarningService.analyze(requestDTO.getCategoryId(), requestDTO);

            if (warning != null && warning.isHasWarning()) {
                requestDTO.setHasWarning(true);
                requestDTO.setWarningLevel(warning.getLevel());
            } else {
                requestDTO.setHasWarning(false);
                requestDTO.setWarningLevel(warning != null ? warning.getLevel() : "NORMAL");
            }
        } else {
            requestDTO.setHasWarning(false);
            requestDTO.setWarningLevel("NORMAL");
        }

        TransactionDTO savedTransaction = transactionService.createTransactionWithDocuments(requestDTO, files, descriptions, currentUser);
        
        if (warning != null && warning.isHasWarning()) {
            riskNotificationService.checkAndNotifySpendingWarning(warning);
        }
        
        TransactionWithWarningDTO result = TransactionWithWarningDTO.builder()
                .transaction(savedTransaction)
                .warning(warning)
                .build();

        String responseMessage = "Tạo giao dịch và upload chứng từ thành công";
        if (warning != null && warning.isHasWarning()) {
            responseMessage = "CRITICAL".equals(warning.getLevel())
                    ? "Tạo giao dịch thành công - ⚠️ Phát hiện chi tiêu bất thường nghiêm trọng!"
                    : "Tạo giao dịch thành công - ⚠️ Phát hiện chi tiêu vượt mức bình thường!";
        }

        return ApiResponse.created(result, responseMessage);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<SpendingWarningDTO>> checkWarningByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Double amount) {
        SpendingWarningDTO warning;
        if (amount != null && amount > 0) {
            TransactionDTO requestDTO = new TransactionDTO();
            requestDTO.setAmount(amount);
            warning = spendingWarningService.analyze(categoryId, requestDTO);
        } else {
            warning = spendingWarningService.analyze(categoryId);
        }
        return ApiResponse.ok(warning, "Phân tích chi tiêu hạng mục thành công");
    }

    // --- 2. CẬP NHẬT GIAO DỊCH (Tạo mới bản ghi, bản cũ thành UPDATED) ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionDTO requestDTO,
            HttpServletRequest request) {

        // Ownership check: Kế toán Thu Chi chỉ được sửa phiếu của chính mình
        User currentUser = getCurrentUser(request);
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            TransactionDTO existing = transactionService.getTransactionById(id);
            if (!isOwnerOrAdmin(currentUser, existing)) {
                return ApiResponse.forbidden("Bạn không có quyền sửa phiếu này. Chỉ được sửa phiếu do chính mình tạo.", "FORBIDDEN_NOT_OWNER");
            }
        }

        return ApiResponse.ok(transactionService.updateTransaction(id, requestDTO), "Cập nhật giao dịch thành công");
    }

    // --- 3. HỦY GIAO DỊCH (Admin chuyển trạng thái thành CANCELLED và hoàn tiền) ---
    // Dùng PATCH vì chúng ta chỉ thay đổi trạng thái chứ không xóa cứng (DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<Void>> cancelTransaction(
            @PathVariable Long id,
            HttpServletRequest request) {

        // Ownership check: Kế toán Thu Chi chỉ được hủy phiếu của chính mình
        User currentUser = getCurrentUser(request);
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            TransactionDTO existing = transactionService.getTransactionById(id);
            if (!isOwnerOrAdmin(currentUser, existing)) {
                return ApiResponse.forbidden("Bạn không có quyền hủy phiếu này. Chỉ được hủy phiếu do chính mình tạo.", "FORBIDDEN_NOT_OWNER");
            }
        }

        transactionService.cancelTransaction(id);
        return ApiResponse.ok(null, "Giao dịch ID: " + id + " đã được hủy và hoàn tiền thành công!");
    }

    // --- 4. LẤY CHI TIẾT 1 GIAO DỊCH ---
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(
            @PathVariable Long id,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);
        TransactionDTO transaction = transactionService.getTransactionById(id);

        // Kế toán Thu Chi chỉ được xem phiếu của chính mình
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            if (!isOwnerOrAdmin(currentUser, transaction)) {
                return ApiResponse.forbidden("Bạn không có quyền xem phiếu này.", "FORBIDDEN_NOT_OWNER");
            }
        }

        return ApiResponse.ok(transaction);
    }

    // --- 5. LẤY DANH SÁCH GIAO DỊCH ---
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<TransactionDTO>>> getAllTransactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transaction_date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        // Kế toán Thu Chi (role=2): bắt buộc lọc theo userId của chính mình,
        // bỏ qua/ghi đè bất kỳ userId nào client gửi lên để chặn xem phiếu của người khác.
        if (currentUser.getRole() != null && currentUser.getRole() == 2) {
            userId = currentUser.getId();
        }

        return ApiResponse.ok(
                PagedResponseDTO.from(
                        transactionService.getAllTransactions(
                                keyword, type, status, fundId, categoryId, partnerId, userId,
                                fromDate, toDate, page, size, sortBy, sortDir)));
    }

    @GetMapping("/total-income")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_THUQUY','ROLE_TONGHOP')")
    public ResponseEntity<ApiResponse<Double>> getTotalIncome() {
        return ApiResponse.ok(transactionService.getTotalIncome());
    }

    @GetMapping("/total-expense")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_THUQUY','ROLE_TONGHOP')")
    public ResponseEntity<ApiResponse<Double>> getTotalExpense() {
        return ApiResponse.ok(transactionService.getTotalExpense());
    }
}