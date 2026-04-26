package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

import java.util.List;

@Service
public interface TransactionService {
    Transaction createTransaction(TransactionDTO requestDTO);
    public Transaction updateTransaction(Long oldId, TransactionDTO newRequest);
    public void cancelTransaction(Long txId);
    Transaction getTransactionById(Long id);
    List<Transaction> getAllTransactions();
}