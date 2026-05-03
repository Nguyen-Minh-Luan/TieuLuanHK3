package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

import java.util.List;
import java.util.Optional;

@Service
public interface TransactionService {
//    Transaction createTransaction(TransactionDTO requestDTO);
//    public Transaction updateTransaction(Long oldId, TransactionDTO newRequest);
//    public void cancelTransaction(Long txId);
//    Transaction getTransactionById(Long id);
//    List<Transaction> getAllTransactions();
//    Double getTotalIncome();
//    Double getTotalExpense();
    TransactionDTO createTransaction(TransactionDTO requestDTO);
    TransactionDTO updateTransaction(Long oldId, TransactionDTO newRequest);
    void cancelTransaction(Long txId);
    TransactionDTO getTransactionById(Long id);
    List<TransactionDTO> getAllTransactions();
    Double getTotalIncome();
    Double getTotalExpense();
}