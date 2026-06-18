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
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.report.ReportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportExportServiceImpl implements PdfReportExportService {

    private final ReportService reportService;
    private final TemplateEngine templateEngine;

    @Value("${voucher.company.name:Công ty TNHH Giải Pháp Tài Chính Việt Nam}")
    private String companyName;

    @Value("${voucher.company.address:Lầu 5, 123 Nguyễn Thị Minh Khai, Quận 3, TP. Hồ Chí Minh}")
    private String companyAddress;

    @Override
    public byte[] generateReportPdf(Long reportId) {
        // 1. Lấy ReportResponseDTO đã tính toán đầy đủ các chỉ tiêu
        ReportResponseDTO report = reportService.getReportById(reportId);

        // 2. Đưa dữ liệu vào Context Thymeleaf
        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);
        context.setVariable("transactions", report.getTransactions());
        context.setVariable("createdByName", report.getCreatedByName());
        context.setVariable("totalFundBalance", report.getCashAndEquivalents());

        // 3. Render Thymeleaf → HTML
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
