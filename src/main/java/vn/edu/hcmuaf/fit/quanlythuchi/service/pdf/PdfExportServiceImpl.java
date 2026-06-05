package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.util.MoneyToWordsConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final TransactionRepository transactionRepository;
    private final TemplateEngine templateEngine;

    @Value("${voucher.company.name:Công ty TNHH Giải Pháp Tài Chính Việt Nam}")
    private String companyName;

    @Value("${voucher.company.address:Lầu 5, 123 Nguyễn Thị Minh Khai, Quận 3, TP. Hồ Chí Minh}")
    private String companyAddress;

    @Override
    public byte[] generateVoucher(Long transactionId) {
        // 1. Fetch transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + transactionId));

        // 2. Build Thymeleaf context
        Context context = new Context();
        context.setVariable("tx", transaction);
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);

        // Convert amount to words
        long amountVal = transaction.getAmount() != null ? transaction.getAmount().longValue() : 0L;
        String amountInWords = MoneyToWordsConverter.convert(amountVal);
        context.setVariable("amountInWords", amountInWords);

        // 3. Process Thymeleaf template to HTML String
        String htmlContent = templateEngine.process("PrintTransaction", context);

        // 4. Determine baseUri safely
        String baseUri = "";
        try {
            java.net.URL resource = PdfExportServiceImpl.class.getResource("/");
            if (resource != null) {
                baseUri = resource.toExternalForm();
            }
        } catch (Exception e) {
            // Ignore and fallback to empty baseUri
        }

        // 5. Render to PDF using OpenHTMLToPDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Programmatically register the Times New Roman font for standard, bold, italic, bold-italic styles
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"), 
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"), 
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"), 
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.ITALIC, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"), 
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.ITALIC, true);

            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(baos);
            builder.run();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi xuất luồng dữ liệu PDF", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hạch toán kết xuất PDF", e);
        }
    }
}
