package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.OriginalDocumentDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Date;
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
    TransactionDTO createTransactionWithDocuments(TransactionDTO requestDTO, List<MultipartFile> files, List<String> descriptions, User currentUser);
    TransactionDTO updateTransaction(Long oldId, TransactionDTO newRequest);
    List<OriginalDocumentDTO> addDocumentsToTransaction(Long transactionId, List<MultipartFile> files, List<String> descriptions, User uploader);
    void cancelTransaction(Long txId);
    TransactionDTO getTransactionById(Long id);
    List<TransactionDTO> getAllTransactions();

    Page<TransactionDTO> getAllTransactions(String keyword, String type, String status,
                                            Long fundId, Long categoryId, Long partnerId, Long userId,
                                            Date fromDate, Date toDate,
                                            int page, int size, String sortBy, String sortDir);
    Double getTotalIncome();
    Double getTotalExpense();
    TransactionDTO toDTO(Transaction tx);
}