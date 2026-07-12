package vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.reconciliation.FundReconciliationRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundReconciliation;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Date;
import java.util.List;

public interface FundReconciliationService {
    Page<FundReconciliation> search(Long fundId, String status, Date fromDate, Date toDate, Pageable pageable);
    
    FundReconciliation getById(Long id);
    
    List<FundReconciliation> createReconciliations(FundReconciliationRequest request, User currentUser);
    
    FundReconciliation updateDraft(Long id, Double actualBalance, String note);
    
    FundReconciliation closeReconciliation(Long id, User currentUser);
    
    FundReconciliation reopenReconciliation(Long id, String reason, User currentUser);
    
    void deleteDraft(Long id);
    
    void assertNotLocked(Long fundId, Date transactionDate);
    
    boolean isLocked(Long fundId, Date transactionDate);
}
