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

import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.OriginalDocumentRepository;

import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final TransactionRepository transactionRepository;
    private final OriginalDocumentRepository originalDocumentRepository;
    private final TemplateEngine templateEngine;

    @Value("${voucher.company.name:Công ty TNHH Giải Pháp Tài Chính Việt Nam}")
    private String companyName;

    @Value("${voucher.company.address:Lầu 5, 123 Nguyễn Thị Minh Khai, Quận 3, TP. Hồ Chí Minh}")
    private String companyAddress;

    @Override
    public byte[] generateVoucher(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + transactionId));

        Context context = new Context();
        context.setVariable("tx", transaction);
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);

        long amountVal = transaction.getAmount() != null ? transaction.getAmount().longValue() : 0L;
        context.setVariable("amountInWords", MoneyToWordsConverter.convert(amountVal));

        List<OriginalDocument> docs = originalDocumentRepository.findByTransaction_Id(transactionId);
        String documentCodes = "";
        if (docs != null && !docs.isEmpty()) {
            documentCodes = docs.stream().map(OriginalDocument::getDocumentCode).collect(Collectors.joining(", "));
        } else if (transaction.getOriginalDocuments() != null && !transaction.getOriginalDocuments().trim().isEmpty()) {
            documentCodes = transaction.getOriginalDocuments();
        }
        context.setVariable("documentCodes", documentCodes);

        String debitAccount = "";
        String creditAccount = "";

        if (transaction.getType() != null) {
            String fundAccount = transaction.getFund() != null && transaction.getFund().getAccountCode() != null 
                ? transaction.getFund().getAccountCode() : "";
            String categoryAccount = transaction.getCategories() != null && transaction.getCategories().getAccountCode() != null 
                ? transaction.getCategories().getAccountCode() : "";

            if (transaction.getType().toUpperCase().contains("INCOME")) {
                debitAccount = fundAccount;
                creditAccount = categoryAccount;
            } else if (transaction.getType().toUpperCase().contains("EXPENSE")) {
                debitAccount = categoryAccount;
                creditAccount = fundAccount;
            }
        }
        
        context.setVariable("debitAccount", debitAccount);
        context.setVariable("creditAccount", creditAccount);

        String htmlContent = templateEngine.process("PrintTransaction", context);

        // ✅ FIX: Dùng Jsoup parse HTML bình thường → convert sang XHTML hợp lệ
        // Jsoup tự xử lý escape & và các ký tự đặc biệt khác
        Document jsoupDoc = Jsoup.parse(htmlContent);
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        String xhtmlContent = jsoupDoc.outerHtml();

        String baseUri = "";
        try {
            java.net.URL resource = PdfExportServiceImpl.class.getResource("/");
            if (resource != null) baseUri = resource.toExternalForm();
        } catch (Exception ignored) {}

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 400, BaseRendererBuilder.FontStyle.ITALIC, true);
            builder.useFont(() -> PdfExportServiceImpl.class.getResourceAsStream("/times.ttf"),
                    "Times New Roman", 700, BaseRendererBuilder.FontStyle.ITALIC, true);

            // ✅ Dùng xhtmlContent đã được Jsoup làm sạch thay vì htmlContent gốc
            builder.withHtmlContent(xhtmlContent, baseUri);
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
