package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.OriginalDocument;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OriginalDocumentDTO {
    private Long id;
    private String documentCode;
    private Long transactionId;
    private String fileName;
    private String contentType;
    private String description;
    private Long uploadedById;
    private String uploadedByName;
    private Date createdAt;

    public static OriginalDocumentDTO fromEntity(OriginalDocument entity) {
        if (entity == null) return null;
        return OriginalDocumentDTO.builder()
                .id(entity.getId())
                .documentCode(entity.getDocumentCode())
                .transactionId(entity.getTransaction() != null ? entity.getTransaction().getId() : null)
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .description(entity.getDescription())
                .uploadedById(entity.getUploadedBy() != null ? entity.getUploadedBy().getId() : null)
                .uploadedByName(entity.getUploadedBy() != null ? entity.getUploadedBy().getFullName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
