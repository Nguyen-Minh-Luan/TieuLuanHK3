package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
@Service
public interface TransactionService {
    Transaction createTransaction(TransactionDTO requestDTO);
    public Transaction updateTransaction(Long oldId, TransactionDTO newRequest);
    public void cancelTransaction(Long txId);
}