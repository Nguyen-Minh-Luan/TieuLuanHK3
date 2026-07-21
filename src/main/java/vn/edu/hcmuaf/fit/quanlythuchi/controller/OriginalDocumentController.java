package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.OriginalDocumentDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.document.OriginalDocumentService;

@RestController
@RequestMapping("/original-documents")
@RequiredArgsConstructor
public class OriginalDocumentController {

    private final OriginalDocumentService documentService;
    private final JwtUtil jwtUtil;

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserFromJwtToken(token);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<OriginalDocumentDTO>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "transactionId", required = false) Long transactionId,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        OriginalDocumentDTO dto = documentService.uploadDocument(file, description, currentUser.getId(), transactionId);
        return ApiResponse.created(dto, "Tải lên chứng từ thành công");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<OriginalDocumentDTO>>> getDocuments(
            @RequestParam(value = "transactionId", required = false) Long transactionId,
            @RequestParam(value = "unlinkedOnly", defaultValue = "false") boolean unlinkedOnly,
            @RequestParam(value = "documentCode", required = false) String documentCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OriginalDocumentDTO> result = documentService.getDocuments(transactionId, unlinkedOnly, documentCode, page,
                size);
        return ApiResponse.ok(PagedResponseDTO.from(result), "Lấy danh sách chứng từ thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OriginalDocumentDTO>> getDocument(@PathVariable Long id) {
        return ApiResponse.ok(documentService.getDocumentById(id), "Lấy thông tin chứng từ thành công");
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getDocumentImage(@PathVariable Long id) {
        OriginalDocument document = documentService.getOriginalDocumentEntity(id);
        if (document.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getContentType()));
        // Có thể thêm Content-Disposition nếu muốn tải về:
        // headers.setContentDispositionFormData("attachment", document.getFileName());

        return new ResponseEntity<>(document.getImageData(), headers, HttpStatus.OK);
    }

    @PatchMapping("/{id}/link/{transactionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<OriginalDocumentDTO>> linkTransaction(
            @PathVariable Long id,
            @PathVariable Long transactionId) {
        OriginalDocumentDTO dto = documentService.linkTransaction(id, transactionId);
        return ApiResponse.ok(dto, "Gắn chứng từ vào giao dịch thành công");
    }

    @PatchMapping("/{id}/unlink")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<OriginalDocumentDTO>> unlinkTransaction(@PathVariable Long id) {
        OriginalDocumentDTO dto = documentService.unlinkTransaction(id);
        return ApiResponse.ok(dto, "Gỡ liên kết chứng từ thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_KETOAN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        documentService.deleteDocument(id, currentUser.getId(), currentUser.getRole());
        return ApiResponse.ok(null, "Xóa chứng từ thành công");
    }
}
