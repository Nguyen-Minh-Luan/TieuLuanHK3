package vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation;

import lombok.Data;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundReconciliation;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class FundReconciliationDTO {
    private Long id;
    private Long fundId;
    private String fundName;
    private String groupCode;
    private Date periodStart;
    private Date periodEnd;
    private Double openingBalanceSystem;
    private Double closingBalanceSystem;
    private Double actualBalance;
    private Double difference;
    private String status;
    private String note;
    private String createdBy;
    private LocalDateTime createdAt;
    private String closedBy;
    private LocalDateTime closedAt;
    private String reopenedBy;
    private LocalDateTime reopenedAt;
    private String reopenReason;

    public static FundReconciliationDTO fromEntity(FundReconciliation entity) {
        FundReconciliationDTO dto = new FundReconciliationDTO();
        dto.setId(entity.getId());
        dto.setFundId(entity.getFund().getId());
        dto.setFundName(entity.getFund().getName());
        dto.setGroupCode(entity.getGroupCode());
        dto.setPeriodStart(entity.getPeriodStart());
        dto.setPeriodEnd(entity.getPeriodEnd());
        dto.setOpeningBalanceSystem(entity.getOpeningBalanceSystem());
        dto.setClosingBalanceSystem(entity.getClosingBalanceSystem());
        dto.setActualBalance(entity.getActualBalance());
        dto.setDifference(entity.getDifference());
        dto.setStatus(entity.getStatus());
        dto.setNote(entity.getNote());
        
        if (entity.getCreatedBy() != null) {
            dto.setCreatedBy(entity.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        
        if (entity.getClosedBy() != null) {
            dto.setClosedBy(entity.getClosedBy().getFullName());
        }
        dto.setClosedAt(entity.getClosedAt());
        
        if (entity.getReopenedBy() != null) {
            dto.setReopenedBy(entity.getReopenedBy().getFullName());
        }
        dto.setReopenedAt(entity.getReopenedAt());
        dto.setReopenReason(entity.getReopenReason());
        
        return dto;
    }
}
