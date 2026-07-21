package vn.edu.hcmuaf.fit.quanlythuchi.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.OriginalDocumentDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.exception.ResourceNotFoundException;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.OriginalDocumentRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.UserRepository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OriginalDocumentServiceImpl implements OriginalDocumentService {

    private final OriginalDocumentRepository originalDocumentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    @Transactional
    public OriginalDocumentDTO uploadDocument(MultipartFile file, String description, Long uploaderId, Long transactionId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user id: " + uploaderId));

        Transaction transaction = null;
        if (transactionId != null) {
            transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy transaction id: " + transactionId));
        }

        OriginalDocument document = buildDocument(file, description, uploader, transaction);
        document = originalDocumentRepository.save(document);
        return OriginalDocumentDTO.fromEntity(document);
    }

    @Override
    public OriginalDocument buildDocument(MultipartFile file, String description, User uploader, Transaction transaction) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ (chỉ nhận jpeg, png, webp)");
        }

        try {
            OriginalDocument document = new OriginalDocument();
            document.setDocumentCode(generateDocumentCode());
            document.setTransaction(transaction);
            document.setImageData(file.getBytes());
            document.setFileName(file.getOriginalFilename());
            document.setContentType(file.getContentType());
            document.setDescription(description);
            document.setUploadedBy(uploader);
            document.setCreatedAt(new Date());
            return document;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file ảnh", e);
        }
    }

    private synchronized String generateDocumentCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String prefix = "CT-" + sdf.format(new Date()) + "-";
        
        Optional<String> lastCodeOpt = originalDocumentRepository.findLastDocumentCodeByPrefix(prefix);
        int nextNumber = 1;
        if (lastCodeOpt.isPresent()) {
            String lastCode = lastCodeOpt.get();
            try {
                String numberPart = lastCode.substring(prefix.length());
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (Exception ignored) {
            }
        }
        return prefix + String.format("%04d", nextNumber);
    }

    @Override
    public OriginalDocumentDTO getDocumentById(Long id) {
        return OriginalDocumentDTO.fromEntity(getOriginalDocumentEntity(id));
    }

    @Override
    public OriginalDocument getOriginalDocumentEntity(Long id) {
        return originalDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ id: " + id));
    }

    @Override
    public Page<OriginalDocumentDTO> getDocuments(Long transactionId, boolean unlinkedOnly, String documentCode, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OriginalDocument> entityPage = originalDocumentRepository.findByFilters(transactionId, unlinkedOnly, documentCode, pageable);
        return entityPage.map(OriginalDocumentDTO::fromEntity);
    }

    @Override
    @Transactional
    public OriginalDocumentDTO linkTransaction(Long documentId, Long transactionId) {
        OriginalDocument document = getOriginalDocumentEntity(documentId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy transaction id: " + transactionId));
        
        document.setTransaction(transaction);
        document = originalDocumentRepository.save(document);
        return OriginalDocumentDTO.fromEntity(document);
    }

    @Override
    @Transactional
    public OriginalDocumentDTO unlinkTransaction(Long documentId) {
        OriginalDocument document = getOriginalDocumentEntity(documentId);
        document.setTransaction(null);
        document = originalDocumentRepository.save(document);
        return OriginalDocumentDTO.fromEntity(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId, Integer userRole) {
        OriginalDocument document = getOriginalDocumentEntity(documentId);
        
        // Kế toán (role = 2) chỉ được xóa chứng từ do mình tạo
        if (userRole != null && userRole == 2) {
            if (document.getUploadedBy() == null || !document.getUploadedBy().getId().equals(userId)) {
                throw new SecurityException("Bạn không có quyền xóa chứng từ này. Chỉ được xóa chứng từ do chính mình tải lên.");
            }
        }
        originalDocumentRepository.delete(document);
    }
}
