package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.pdf.PdfExportService;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pdf")
public class VoucherController {

        private final PdfExportService pdfExportService;
        private final TransactionRepository transactionRepository;

        @GetMapping("/transactions/{id}")
        public ResponseEntity<byte[]> exportVoucherPdf(@PathVariable Long id) {
                Transaction tx = transactionRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + id));

                byte[] pdfBytes = pdfExportService.generateVoucher(id);

                String prefix = "INCOME".equalsIgnoreCase(tx.getType()) ? "phieu-thu-" : "phieu-chi-";
                String filename = prefix + tx.getTransaction_code() + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build());
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(pdfBytes);
        }
}
