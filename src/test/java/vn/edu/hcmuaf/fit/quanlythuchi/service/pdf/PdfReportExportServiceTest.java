package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ReportResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.report.ReportService;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfReportExportServiceTest {

    @Mock
    private ReportService reportService;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private PdfReportExportServiceImpl pdfReportExportService;

    private ReportResponseDTO reportDTO;

    @BeforeEach
    void setUp() {
        reportDTO = ReportResponseDTO.builder()
                .id(1L)
                .title("Báo cáo Q1 2026")
                .type("QUARTERLY")
                .fromDate(new Date())
                .toDate(new Date())
                .totalIncome(1000.0)
                .totalExpense(500.0)
                .netBalance(500.0)
                .createdBy(1L)
                .createdByName("Test User")
                .createdAt(new Date())
                .cashAndEquivalents(15000.0)
                .transactions(new ArrayList<>())
                .build();
    }

    @Test
    void generateReportPdf_Success() {
        when(reportService.getReportById(1L)).thenReturn(reportDTO);
        
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
        when(reportService.getReportById(2L)).thenThrow(new RuntimeException("Không tìm thấy báo cáo với ID: 2"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            pdfReportExportService.generateReportPdf(2L);
        });

        assertTrue(exception.getMessage().contains("Không tìm thấy báo cáo với ID"));
    }
}
