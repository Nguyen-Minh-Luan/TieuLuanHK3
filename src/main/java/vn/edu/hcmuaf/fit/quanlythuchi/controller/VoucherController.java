package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ReportRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.pdf.PdfExportService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.pdf.PdfReportExportService;
import vn.edu.hcmuaf.fit.quanlythuchi.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pdf")
public class VoucherController {

        private final PdfExportService pdfExportService;
        private final PdfReportExportService pdfReportExportService;
        private final TransactionRepository transactionRepository;
        private final ReportRepository reportRepository;

        @GetMapping("/transactions/{id}")
        public ResponseEntity<byte[]> exportVoucherPdf(@PathVariable Long id) {
                Transaction tx = transactionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giao dịch với ID: " + id));

                byte[] pdfBytes = pdfExportService.generateVoucher(id);

                String prefix = "INCOME".equalsIgnoreCase(tx.getType()) ? "phieu-thu-" : "phieu-chi-";
                String transactionCode = tx.getTransaction_code() != null ? tx.getTransaction_code() : String.valueOf(tx.getId());
                String filename = prefix + transactionCode + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.attachment()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build());
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(pdfBytes);
        }

        @GetMapping("/transactions/{id}/preview")
        public ResponseEntity<byte[]> previewVoucherPdf(@PathVariable Long id) {
                Transaction tx = transactionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Giao dịch với ID: " + id));

                byte[] pdfBytes = pdfExportService.generateVoucher(id);

                String prefix = "INCOME".equalsIgnoreCase(tx.getType()) ? "phieu-thu-" : "phieu-chi-";
                String transactionCode = tx.getTransaction_code() != null ? tx.getTransaction_code() : String.valueOf(tx.getId());
                String filename = prefix + transactionCode + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.inline()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build());
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(pdfBytes);
        }

        @GetMapping("/reports/{id}")
        public ResponseEntity<byte[]> exportReportPdf(@PathVariable Long id) {
                Report report = reportRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo với ID: " + id));

                byte[] pdfBytes = pdfReportExportService.generateReportPdf(id);

                String filename = "bao-cao-tai-chinh-" + report.getId() + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.attachment()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build());
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(pdfBytes);
        }
}
