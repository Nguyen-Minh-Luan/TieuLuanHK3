package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.DebtDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.debt.DebtService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    /** POST /debts — Tạo khoản nợ mới */
    @PostMapping
    public ResponseEntity<ApiResponse<DebtDTO>> createDebt(@RequestBody DebtDTO request) {
        return ApiResponse.created(debtService.createDebt(request), "Tạo khoản nợ thành công");
    }

    /** PATCH /debts/{id} — Cập nhật khoản nợ */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtDTO>> updateDebt(
            @PathVariable Long id, @RequestBody DebtDTO request) {
        return ApiResponse.ok(debtService.updateDebt(id, request), "Cập nhật khoản nợ thành công");
    }

    /** DELETE /debts/{id} — Xóa mềm khoản nợ */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ApiResponse.ok(null, "Xóa khoản nợ thành công");
    }

    /** GET /debts/{id} — Lấy chi tiết một khoản nợ */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DebtDTO>> getDebtById(@PathVariable Long id) {
        return ApiResponse.ok(debtService.getDebtById(id));
    }

    /** GET /debts — Lấy danh sách đối tác có phân trang và tìm kiếm */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<DebtDTO>>> getAllDebts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String debtType,
            @RequestParam(required = false) Boolean isPaid,
            @RequestParam(defaultValue = "1")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        debtService.getAllDebts(keyword, debtType, isPaid, page, size, sortBy, sortDir)));
    }

    /** GET /debts/type/{debtType} — Lấy theo loại (RECEIVABLE | PAYABLE) */
    @GetMapping("/type/{debtType}")
    public ResponseEntity<ApiResponse<List<DebtDTO>>> getDebtsByType(
            @PathVariable String debtType) {
        return ApiResponse.ok(debtService.getDebtsByType(debtType));
    }

    /** GET /debts/partner/{partnerId} — Lấy theo đối tác */
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<DebtDTO>>> getDebtsByPartner(
            @PathVariable Long partnerId) {
        return ApiResponse.ok(debtService.getDebtsByPartner(partnerId));
    }

    /** GET /debts/unpaid — Lấy tất cả khoản nợ chưa thanh toán xong */
    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<DebtDTO>>> getUnpaidDebts() {
        return ApiResponse.ok(debtService.getUnpaidDebts());
    }

    /** GET /debts/unpaid/{debtType} — Lấy nợ chưa trả theo loại */
    @GetMapping("/unpaid/{debtType}")
    public ResponseEntity<ApiResponse<List<DebtDTO>>> getUnpaidDebtsByType(
            @PathVariable String debtType) {
        return ApiResponse.ok(debtService.getUnpaidDebtsByType(debtType));
    }

    /** GET /debts/summary — Tổng hợp số tiền còn phải thu/trả */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getDebtSummary() {
        return ApiResponse.ok(debtService.getDebtSummary());
    }
}
