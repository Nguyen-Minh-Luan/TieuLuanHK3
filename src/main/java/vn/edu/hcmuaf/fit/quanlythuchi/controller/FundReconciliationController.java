package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation.FundReconciliationDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation.FundReconciliationRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundReconciliation;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.auth.AuthService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation.FundReconciliationService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reconciliations")
@RequiredArgsConstructor
public class FundReconciliationController {

    private final FundReconciliationService reconciliationService;
    private final JwtUtil jwtUtil;
    private final AuthService authService; // to get full user by username

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserFromJwtToken(token);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FundReconciliationDTO>>> getAll(
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "periodEnd") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<FundReconciliation> result = reconciliationService.search(fundId, status, fromDate, toDate, pageable);
        Page<FundReconciliationDTO> dtos = result.map(FundReconciliationDTO::fromEntity);

        return ApiResponse.ok(dtos, "Thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FundReconciliationDTO>> getById(@PathVariable Long id) {
        FundReconciliation recon = reconciliationService.getById(id);
        return ApiResponse.ok(FundReconciliationDTO.fromEntity(recon), "Thành công");
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY')")
    public ResponseEntity<ApiResponse<List<FundReconciliationDTO>>> create(
            @RequestBody FundReconciliationRequest requestBody,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<FundReconciliation> created = reconciliationService.createReconciliations(requestBody, currentUser);
        List<FundReconciliationDTO> dtos = created.stream().map(FundReconciliationDTO::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos, "Tạo phiên kiểm kê thành công");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY')")
    public ResponseEntity<ApiResponse<FundReconciliationDTO>> updateDraft(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        Double actualBalance = updates.containsKey("actualBalance")
                ? Double.valueOf(updates.get("actualBalance").toString())
                : null;
        String note = updates.containsKey("note") ? updates.get("note").toString() : null;

        FundReconciliation updated = reconciliationService.updateDraft(id, actualBalance, note);
        return ApiResponse.ok(FundReconciliationDTO.fromEntity(updated), "Cập nhật thành công");
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY')")
    public ResponseEntity<ApiResponse<FundReconciliationDTO>> close(
            @PathVariable Long id,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        FundReconciliation closed = reconciliationService.closeReconciliation(id, currentUser);
        return ApiResponse.ok(FundReconciliationDTO.fromEntity(closed), "Chốt kiểm kê thành công");
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<FundReconciliationDTO>> reopen(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        String reason = body.get("reason");
        FundReconciliation reopened = reconciliationService.reopenReconciliation(id, reason, currentUser);
        return ApiResponse.ok(FundReconciliationDTO.fromEntity(reopened), "Mở khóa thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        reconciliationService.deleteDraft(id);
        return ApiResponse.ok("", "Xóa phiên kiểm kê nháp thành công");
    }

    @GetMapping("/lock-check")
    public ResponseEntity<ApiResponse<Boolean>> checkLock(
            @RequestParam Long fundId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        boolean isLocked = reconciliationService.isLocked(fundId, date);
        return ApiResponse.ok(isLocked, "Thành công");
    }
}
