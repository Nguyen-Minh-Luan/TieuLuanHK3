package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

public interface PdfReportExportService {
    byte[] generateReportPdf(Long reportId);
}
