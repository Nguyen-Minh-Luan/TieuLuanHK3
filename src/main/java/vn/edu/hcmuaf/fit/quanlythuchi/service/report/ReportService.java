package vn.edu.hcmuaf.fit.quanlythuchi.service.report;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportResponseDTO;

import java.util.Date;
import java.util.List;

@Service
public interface ReportService {

    /** Tạo báo cáo mới. Tự động tính totalIncome, totalExpense, netBalance từ transactions. */
    ReportResponseDTO createReport(ReportDTO request);

    /** Cập nhật thông tin báo cáo (title, note, status...). Có thể tái tính số liệu. */
    ReportResponseDTO updateReport(Long id, ReportDTO request);

    /** Xóa mềm báo cáo */
    void deleteReport(Long id);

    /** Lấy chi tiết một báo cáo */
    ReportResponseDTO getReportById(Long id);

    /** Lấy tất cả báo cáo chưa bị xóa */
    List<ReportResponseDTO> getAllReports();

    Page<ReportResponseDTO> getAllReports(String keyword, String type, String status,
                                          Long createdBy, Date fromDate, Date toDate,
                                          int page, int size, String sortBy, String sortDir);

    /** Lấy báo cáo theo loại */
    List<ReportResponseDTO> getReportsByType(String type);

    /** Lấy báo cáo theo người tạo */
    List<ReportResponseDTO> getReportsByUser(Long userId);

    /**
     * Tái tính số liệu (totalIncome, totalExpense, netBalance) cho báo cáo
     * dựa trên dữ liệu thực tế trong bảng transactions tại thời điểm gọi.
     */
    ReportResponseDTO recalculate(Long id);
}