package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ReportRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfReportExportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private FundRepository fundRepository;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private PdfReportExportServiceImpl pdfReportExportService;

    private Report report;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Test User");

        report = new Report();
        report.setId(1L);
        report.setTitle("Báo cáo Q1 2026");
        report.setType("QUARTERLY");
        report.setFromDate(new Date());
        report.setToDate(new Date());
        report.setTotalIncome(1000.0);
        report.setTotalExpense(500.0);
        report.setNetBalance(500.0);
        report.setCreatedBy(user);
        report.setCreatedAt(new Date());
    }

    @Test
    void generateReportPdf_Success() {
        when(reportRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(report));
        when(reportRepository.findTransactionsByDateRange(any(), any())).thenReturn(new ArrayList<>());
        when(fundRepository.getTotalFundBalance()).thenReturn(Optional.of(15000.0));
        
        // Return a valid simple XHTML string
        String simpleHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head><title>Test</title></head>\n" +
                "<body><h1>Test PDF</h1></body>\n" +
                "</html>";
        when(templateEngine.process(eq("printReport"), any(Context.class))).thenReturn(simpleHtml);

        byte[] result = pdfReportExportService.generateReportPdf(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateReportPdf_ReportNotFound() {
        when(reportRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            pdfReportExportService.generateReportPdf(2L);
        });

        assertTrue(exception.getMessage().contains("Không tìm thấy báo cáo với ID"));
    }
}
