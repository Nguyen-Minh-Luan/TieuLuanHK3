package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.fund.FundService;

import java.util.Map;

@RestController
@RequestMapping("/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    /** GET /funds — Lấy danh sách quỹ tiền có phân trang và tìm kiếm */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<FundDTO>>> getAllFunds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        fundService.getAllFunds(keyword, type, status, page, size, sortBy, sortDir)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FundDTO>> createFund(@RequestBody FundDTO request) {
        return ApiResponse.created(fundService.createFund(request), "Tạo nguồn tiền thành công");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FundDTO>> updateFund(
            @PathVariable Long id,
            @RequestBody FundDTO request) {
        return ApiResponse.ok(fundService.updateFund(id, request), "Cập nhật nguồn tiền thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFund(@PathVariable Long id) {
        fundService.deleteFund(id);
        return ApiResponse.ok(null, "Xóa nguồn tiền thành công");
    }

    @GetMapping("/total-balance")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getTotalFundBalance() {
        Double totalBalance = fundService.getTotalFund();
        return ApiResponse.ok(Map.of("totalBalance", totalBalance));
    }
}
