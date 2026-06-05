package vn.edu.hcmuaf.fit.quanlythuchi.service.pdf;

public interface PdfExportService {
    byte[] generateVoucher(Long transactionId);
}
