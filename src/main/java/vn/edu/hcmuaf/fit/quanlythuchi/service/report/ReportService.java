package vn.edu.hcmuaf.fit.quanlythuchi.service.report;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportDTO;

import java.util.List;

@Service
public interface ReportService {

    /** Tạo báo cáo mới. Tự động tính totalIncome, totalExpense, netBalance từ transactions. */
    ReportDTO createReport(ReportDTO request);

    /** Cập nhật thông tin báo cáo (title, note, status...). Có thể tái tính số liệu. */
    ReportDTO updateReport(Long id, ReportDTO request);

    /** Xóa mềm báo cáo */
    void deleteReport(Long id);

    /** Lấy chi tiết một báo cáo */
    ReportDTO getReportById(Long id);

    /** Lấy tất cả báo cáo chưa bị xóa */
    List<ReportDTO> getAllReports();

    /** Lấy báo cáo theo loại */
    List<ReportDTO> getReportsByType(String type);

    /** Lấy báo cáo theo người tạo */
    List<ReportDTO> getReportsByUser(Long userId);

    /**
     * Tái tính số liệu (totalIncome, totalExpense, netBalance) cho báo cáo
     * dựa trên dữ liệu thực tế trong bảng transactions tại thời điểm gọi.
     */
    ReportDTO recalculate(Long id);
}