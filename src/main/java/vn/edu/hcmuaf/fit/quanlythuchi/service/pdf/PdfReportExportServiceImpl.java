package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ReportRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportExportServiceImpl implements PdfReportExportService {

    private final ReportRepository reportRepository;
    private final FundRepository fundRepository;
    private final TemplateEngine templateEngine;

    @Value("${voucher.company.name:Công ty TNHH Giải Pháp Tài Chính Việt Nam}")
    private String companyName;

    @Value("${voucher.company.address:Lầu 5, 123 Nguyễn Thị Minh Khai, Quận 3, TP. Hồ Chí Minh}")
    private String companyAddress;

    @Override
    public byte[] generateReportPdf(Long reportId) {
        // 1. Lấy Report entity
        Report report = reportRepository.findByIdAndIsDeletedFalse(reportId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + reportId));

        // 2. Lấy danh sách giao dịch trong kỳ
        List<Transaction> transactions = reportRepository.findTransactionsByDateRange(
                report.getFromDate(), report.getToDate());

        // 3. Tính tổng tiền quỹ hiện tại (dùng cho chỉ tiêu "Tiền và tương đương tiền" - mã 110/111)
        Double totalFundBalance = fundRepository.getTotalFundBalance().orElse(0.0);

        // 4. Đưa dữ liệu vào Context Thymeleaf
        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);
        context.setVariable("transactions", transactions);
        context.setVariable("createdByName",
                report.getCreatedBy() != null ? report.getCreatedBy().getFullName() : "");
        context.setVariable("totalFundBalance", totalFundBalance);

        // 5. Render Thymeleaf → HTML
        String htmlContent = templateEngine.process("printReport", context);

        // 6. Jsoup parse → XHTML (giống PdfExportServiceImpl)
        Document jsoupDoc = Jsoup.parse(htmlContent);
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtmlContent = jsoupDoc.outerHtml();

        // 7. Xác định baseUri để resolve resource tương đối
        String baseUri = "";
        try {
            java.net.URL resource = PdfReportExportServiceImpl.class.getResource("/");
            if (resource != null) baseUri = resource.toExternalForm();
        } catch (Exception ignored) {}

        // 8. PdfRendererBuilder → byte[]
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Tái sử dụng font times.ttf đã có, không cần thêm font mới
            builder.useFont(() -> PdfReportExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfReportExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfReportExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.ITALIC, true);
            builder.useFont(() -> PdfReportExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.ITALIC, true);
            builder.withHtmlContent(xhtmlContent, baseUri);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi xuất luồng dữ liệu PDF báo cáo", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kết xuất PDF báo cáo tài chính", e);
        }
    }
}
