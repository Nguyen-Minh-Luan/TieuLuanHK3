package vn.edu.hcmuaf.fit.quanlythuchi.service.document;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.OriginalDocumentDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.List;

public interface OriginalDocumentService {

    OriginalDocumentDTO uploadDocument(MultipartFile file, String description, Long uploaderId, Long transactionId);

    OriginalDocument buildDocument(MultipartFile file, String description, User uploader, Transaction transaction);

    OriginalDocumentDTO getDocumentById(Long id);

    OriginalDocument getOriginalDocumentEntity(Long id);

    Page<OriginalDocumentDTO> getDocuments(Long transactionId, boolean unlinkedOnly, String documentCode, int page, int size);

    OriginalDocumentDTO linkTransaction(Long documentId, Long transactionId);

    OriginalDocumentDTO unlinkTransaction(Long documentId);

    void deleteDocument(Long documentId, Long userId, Integer userRole);
}
