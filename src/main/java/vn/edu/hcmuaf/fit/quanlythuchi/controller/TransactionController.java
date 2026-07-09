package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.SpendingWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionWithWarningDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.warning.SpendingWarningService;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SpendingWarningService spendingWarningService;

    // --- 1. TẠO MỚI GIAO DỊCH ---
    // @PostMapping
    // public ResponseEntity<Transaction> createTransaction(@RequestBody
    // TransactionDTO requestDTO) {
    // Transaction newTransaction =
    // transactionService.createTransaction(requestDTO);
    // return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    // }
    // --- 1. TẠO MỚI GIAO DỊCH (tích hợp cảnh báo chi tiêu thông minh) ---
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionWithWarningDTO>> createTransaction(
            @RequestBody TransactionDTO requestDTO) {

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
    // @PutMapping("/{id}")
    // public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id,
    // @RequestBody TransactionDTO requestDTO) {
    // Transaction updatedTransaction = transactionService.updateTransaction(id,
    // requestDTO);
    // return ResponseEntity.ok(updatedTransaction);
    // }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionDTO requestDTO) {
        return ApiResponse.ok(transactionService.updateTransaction(id, requestDTO), "Cập nhật giao dịch thành công");
    }

    // --- 3. HỦY GIAO DỊCH (Admin chuyển trạng thái thành CANCELLED và hoàn tiền)
    // ---
    // Dùng PATCH vì chúng ta chỉ thay đổi trạng thái chứ không xóa cứng (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
        return ApiResponse.ok(null, "Giao dịch ID: " + id + " đã được hủy và hoàn tiền thành công!");
    }

    // --- 4. LẤY CHI TIẾT 1 GIAO DỊCH ---
    // @GetMapping("/{id}")
    // public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id)
    // {
    // // Giả định bạn đã viết hàm getTransactionById trong ServiceImpl
    // Transaction transaction = transactionService.getTransactionById(id);
    // return ResponseEntity.ok(transaction);
    // }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        return ApiResponse.ok(transactionService.getTransactionById(id));
    }

    // --- 5. LẤY DANH SÁCH GIAO DỊCH ---
    // @GetMapping
    // public ResponseEntity<List<Transaction>> getAllTransactions() {
    // // Giả định bạn đã viết hàm getAllTransactions trong ServiceImpl
    // List<Transaction> transactions = transactionService.getAllTransactions();
    // return ResponseEntity.ok(transactions);
    // }
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
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        transactionService.getAllTransactions(
                                keyword, type, status, fundId, categoryId, partnerId, userId,
                                fromDate, toDate, page, size, sortBy, sortDir)));
    }

    @GetMapping("/total-income")
    public ResponseEntity<ApiResponse<Double>> getTotalIncome() {
        return ApiResponse.ok(transactionService.getTotalIncome());
    }

    @GetMapping("/total-expense")
    public ResponseEntity<ApiResponse<Double>> getTotalExpense() {
        return ApiResponse.ok(transactionService.getTotalExpense());
    }
}