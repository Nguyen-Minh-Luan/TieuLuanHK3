package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.TransactionStatus;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation.FundReconciliationService;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.OriginalDocumentRepository;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private FundRepository fundRepository;
    
    @Mock
    private FundReconciliationService reconciliationService;

    @Mock
    private OriginalDocumentRepository originalDocumentRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void updateTransaction_shouldIgnoreClientProvidedStatus() {
        // Arrange
        Long existingActiveTxId = 1L;
        Fund fund = new Fund();
        fund.setId(10L);
        fund.setCurrentBalance(1000.0);
        
        Transaction oldTx = new Transaction();
        oldTx.setId(existingActiveTxId);
        oldTx.setStatus(TransactionStatus.ACTIVE);
        oldTx.setFund(fund);
        oldTx.setTransaction_date(new Date());
        oldTx.setType("EXPENSE");
        oldTx.setAmount(100.0);
        
        TransactionDTO req = new TransactionDTO();
        req.setStatus("CANCELLED"); // cố tình gửi status lạ
        req.setAmount(100.0);
        req.setType("EXPENSE");
        
        when(transactionRepository.findById(existingActiveTxId)).thenReturn(Optional.of(oldTx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            if (saved.getStatus() == TransactionStatus.UPDATED) return saved;
            // Fake id for new tx
            saved.setId(2L);
            return saved;
        });
        
        // Act
        TransactionDTO result = transactionService.updateTransaction(existingActiveTxId, req);
        
        // Assert
        assertEquals("ACTIVE", result.getStatus()); // bản ghi mới luôn ACTIVE
    }
}
