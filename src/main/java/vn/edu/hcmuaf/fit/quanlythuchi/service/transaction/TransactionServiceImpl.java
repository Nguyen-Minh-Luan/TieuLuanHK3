package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final FundRepository fundRepository;
    // Cần inject thêm CategoryRepository, UserRepository... tùy thực tế dự án

    @Override
    @Transactional
    public Transaction createTransaction(TransactionDTO request) {
        // 1. Kiểm tra ngoại lệ đầu vào cơ bản
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0");
        }
        if (!"INCOME".equalsIgnoreCase(request.getType()) && !"EXPENSE".equalsIgnoreCase(request.getType())) {
            throw new IllegalArgumentException("Loại giao dịch không hợp lệ (Chỉ nhận INCOME hoặc EXPENSE)");
        }

        // 2. Lấy Fund và kiểm tra ngoại lệ nếu không tồn tại
        Fund fund = fundRepository.findById(request.getFundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nguồn tiền (Fund) với ID: " + request.getFundId()));

        // 3. Xử lý logic cộng trừ số dư (Tuần tự, không đệ quy)
        Double currentBalance = fund.getCurrentBalance() != null ? fund.getCurrentBalance() : 0.0;

        if ("INCOME".equalsIgnoreCase(request.getType())) {
            fund.setCurrentBalance(currentBalance + request.getAmount());
        } else {
            if (currentBalance < request.getAmount()) {
                throw new RuntimeException("Số dư trong nguồn tiền không đủ để thực hiện Phiếu Chi!");
            }
            fund.setCurrentBalance(currentBalance - request.getAmount());
        }

        // Cập nhật số dư Fund
        fundRepository.save(fund);

        // 4. Map DTO sang Entity và lưu Transaction
        Transaction transaction = new Transaction();
        transaction.setFund(fund);
        // ... (Map các entity khác như Category, User, Partner tương tự như trên) ...

        transaction.setType(request.getType().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());

        // Tạo mã giao dịch ngẫu nhiên
        transaction.setTransaction_code("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setTransaction_date(new Date());
        transaction.setCreated_at(new Date());
        transaction.setDatetime(new Date());
        transaction.setStatus("COMPLETED");

        return transactionRepository.save(transaction);
    }
    @Override
    @Transactional
    public Transaction updateTransaction(Long oldId, TransactionDTO newRequest) {
        Transaction oldTx = transactionRepository.findById(oldId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // 1. Hoàn tác số dư của giao dịch cũ
        Fund fund = oldTx.getFund();
        if ("INCOME".equals(oldTx.getType())) {
            fund.setCurrentBalance(fund.getCurrentBalance() - oldTx.getAmount());
        } else {
            fund.setCurrentBalance(fund.getCurrentBalance() + oldTx.getAmount());
        }

        // 2. Đánh dấu bản ghi cũ là đã được cập nhật (để lưu vết)
        oldTx.setStatus("UPDATED");
        transactionRepository.save(oldTx);

        // 3. Tạo bản ghi mới (Create new instead of edit)
        Transaction newTx = new Transaction();
        // Copy dữ liệu từ Request, thiết lập parentId = oldId
        newTx.setParentId(oldId);
        newTx.setStatus("ACTIVE");
        // ... set các trường khác ...

        // 4. Cập nhật số dư theo số tiền mới
        if ("INCOME".equals(newTx.getType())) {
            fund.setCurrentBalance(fund.getCurrentBalance() + newTx.getAmount());
        } else {
            fund.setCurrentBalance(fund.getCurrentBalance() - newTx.getAmount());
        }

        fundRepository.save(fund);
        return transactionRepository.save(newTx);
    }
    @Override
    @Transactional
    //hàm cancel giao dịch của admin
    public void cancelTransaction(Long txId) {
        Transaction tx = transactionRepository.findById(txId).get();

        tx.setStatus("CANCELLED");

        // Hoàn lại tiền vào quỹ
        Fund fund = tx.getFund();
        if ("INCOME".equals(tx.getType())) {
            fund.setCurrentBalance(fund.getCurrentBalance() - tx.getAmount());
        } else {
            fund.setCurrentBalance(fund.getCurrentBalance() + tx.getAmount());
        }

        fundRepository.save(fund);
        transactionRepository.save(tx);
    }
}