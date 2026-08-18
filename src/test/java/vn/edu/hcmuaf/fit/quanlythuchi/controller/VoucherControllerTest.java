package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.hcmuaf.fit.quanlythuchi.config.SecurityConfig;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtAuthenticationFilter;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ReportRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.pdf.PdfExportService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.pdf.PdfReportExportService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
@Import(SecurityConfig.class) // Import SecurityConfig if needed to handle custom security
public class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfExportService pdfExportService;

    @MockBean
    private PdfReportExportService pdfReportExportService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private ReportRepository reportRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // mock to avoid JWT errors

    @Test
    @WithMockUser
    public void testPreviewReportPdf() throws Exception {
        Report mockReport = new Report();
        mockReport.setId(10L);

        byte[] mockPdfBytes = new byte[]{1, 2, 3};

        when(reportRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(mockReport));
        when(pdfReportExportService.generateReportPdf(10L)).thenReturn(mockPdfBytes);

        mockMvc.perform(get("/pdf/reports/10/preview"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"bao-cao-tai-chinh-10.pdf\""))
                .andExpect(content().bytes(mockPdfBytes));
    }
}
