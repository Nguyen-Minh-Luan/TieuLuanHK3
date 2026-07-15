package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transfer.FundTransferService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/fund-transfers")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;
    private final JwtUtil jwtUtil;

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserFromJwtToken(token);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY')")
    public ResponseEntity<ApiResponse<FundTransferDTO>> transferFund(
            @RequestBody FundTransferRequest requestBody,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        FundTransferDTO result = fundTransferService.transferFund(requestBody, currentUser);
        return ApiResponse.ok(result, "Chuyển quỹ thành công");
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY', 'ROLE_TONGHOP')")
    public ResponseEntity<ApiResponse<Page<FundTransferDTO>>> getTransferHistory(
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        LocalDateTime fromDateLocal = fromDate != null ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime toDateLocal = toDate != null ? toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(1).minusNanos(1) : null;

        Page<FundTransferDTO> result = fundTransferService.getTransferHistory(fundId, fromDateLocal, toDateLocal, createdBy, page, size, sortBy, sortDir);
        return ApiResponse.ok(result, "Lấy lịch sử chuyển quỹ thành công");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THUQUY', 'ROLE_TONGHOP')")
    public ResponseEntity<ApiResponse<FundTransferDTO>> getTransferById(@PathVariable Long id) {
        FundTransferDTO result = fundTransferService.getTransferById(id);
        return ApiResponse.ok(result, "Thành công");
    }
}
