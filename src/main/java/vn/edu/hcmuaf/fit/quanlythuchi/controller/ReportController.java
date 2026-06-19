package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.report.ReportService;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Tạo báo cáo mới. Số liệu (totalIncome, totalExpense, netBalance)
     * được tự động tính từ bảng transactions theo khoảng fromDate–toDate.
     * POST /reports
     * Body: { title, type, fromDate, toDate, note, status, createdBy }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponseDTO>> createReport(@RequestBody ReportDTO request) {
        return ApiResponse.created(reportService.createReport(request), "Tạo báo cáo thành công");
    }

    /**
     * Cập nhật thông tin báo cáo. Nếu thay đổi fromDate/toDate, số liệu sẽ được tái tính tự động.
     * PATCH /reports/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> updateReport(
            @PathVariable Long id,
            @RequestBody ReportDTO request) {
        return ApiResponse.ok(reportService.updateReport(id, request), "Cập nhật báo cáo thành công");
    }

    /**
     * Xóa mềm báo cáo (chuyển isDeleted = true).
     * DELETE /reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ApiResponse.ok(null, "Xóa báo cáo thành công");
    }

    /**
     * Lấy chi tiết một báo cáo.
     * GET /reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> getReportById(@PathVariable Long id) {
        return ApiResponse.ok(reportService.getReportById(id));
    }

    /**
     * Lấy toàn bộ danh sách báo cáo chưa bị xóa (phân trang & tìm kiếm).
     * GET /reports
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<ReportResponseDTO>>> getAllReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam(defaultValue = "1")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {
        return ApiResponse.ok(
            PagedResponseDTO.from(
                reportService.getAllReports(
                    keyword, type, status, createdBy, fromDate, toDate,
                    page, size, sortBy, sortDir)));
    }

    /**
     * Lấy danh sách báo cáo theo loại.
     * GET /reports/type/{type}   (type = MONTHLY | QUARTERLY | YEARLY | CUSTOM)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<ReportResponseDTO>>> getReportsByType(@PathVariable String type) {
        return ApiResponse.ok(reportService.getReportsByType(type));
    }

    /**
     * Lấy danh sách báo cáo do một user tạo.
     * GET /reports/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReportResponseDTO>>> getReportsByUser(@PathVariable Long userId) {
        return ApiResponse.ok(reportService.getReportsByUser(userId));
    }

    /**
     * Tái tính số liệu cho báo cáo dựa trên dữ liệu transactions hiện tại.
     * Hữu ích khi dữ liệu giao dịch thay đổi sau khi báo cáo đã được tạo.
     * POST /reports/{id}/recalculate
     */
    @PostMapping("/{id}/recalculate")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> recalculate(@PathVariable Long id) {
        return ApiResponse.ok(reportService.recalculate(id), "Tái tính số liệu báo cáo thành công");
    }
}