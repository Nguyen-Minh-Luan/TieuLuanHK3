package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // --- 1. TẠO MỚI GIAO DỊCH ---
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionDTO requestDTO) {
        Transaction newTransaction = transactionService.createTransaction(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    }

    // --- 2. CẬP NHẬT GIAO DỊCH (Tạo mới bản ghi, bản cũ thành UPDATED) ---
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody TransactionDTO requestDTO) {
        Transaction updatedTransaction = transactionService.updateTransaction(id, requestDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    // --- 3. HỦY GIAO DỊCH (Admin chuyển trạng thái thành CANCELLED và hoàn tiền) ---
    // Dùng PATCH vì chúng ta chỉ thay đổi trạng thái chứ không xóa cứng (DELETE)
    @PatchMapping("/{id}")
    public ResponseEntity<String> cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
        return ResponseEntity.ok("Giao dịch ID: " + id + " đã được hủy và hoàn tiền thành công!");
    }

    // --- 4. LẤY CHI TIẾT 1 GIAO DỊCH ---
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        // Giả định bạn đã viết hàm getTransactionById trong ServiceImpl
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    // --- 5. LẤY DANH SÁCH GIAO DỊCH ---
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        // Giả định bạn đã viết hàm getAllTransactions trong ServiceImpl
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}