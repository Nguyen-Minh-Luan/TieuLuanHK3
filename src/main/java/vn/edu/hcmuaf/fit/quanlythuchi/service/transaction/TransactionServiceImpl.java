package vn.edu.hcmuaf.fit.quanlythuchi.service.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.TransactionDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.*;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.*;
import vn.edu.hcmuaf.fit.quanlythuchi.service.transaction.TransactionService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final FundRepository fundRepository;
    // Cần inject thêm CategoryRepository, UserRepository... tùy thực tế dự án
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
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

        // 4. Khởi tạo và Map DTO sang Entity
        Transaction transaction = new Transaction();

        // --- BẮT ĐẦU GẮN CÁC ENTITY LIÊN QUAN ---

        // Gắn Fund
        transaction.setFund(fund);

        // Lấy và gắn Category (Bắt buộc)
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục (Category) với ID: " + request.getCategoryId()));
        transaction.setCategories(category);

        // Lấy và gắn User (Bắt buộc - Người thực hiện giao dịch)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng (User) với ID: " + request.getUserId()));
        transaction.setUser(user);

        // Lấy và gắn Partner (KHÔNG bắt buộc - Chỉ query khi client có gửi lên partnerId)
        if (request.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác (Partner) với ID: " + request.getPartnerId()));
            transaction.setPartner(partner);
        }

        // --- KẾT THÚC GẮN ENTITY ---

        // Gắn các thuộc tính cơ bản
        transaction.setType(request.getType().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());

        // Tạo mã giao dịch ngẫu nhiên
        transaction.setTransaction_code("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setTransaction_date(new Date());
        transaction.setCreated_at(new Date());
        transaction.setDatetime(new Date());
        transaction.setStatus("ACTIVE"); // Đổi thành ACTIVE cho đồng bộ với logic Update của bạn ở trên

        return transactionRepository.save(transaction);
    }
    @Override
    @Transactional
    public Transaction updateTransaction(Long oldId, TransactionDTO newRequest) {
        // Lấy giao dịch cũ lên
        Transaction oldTx = transactionRepository.findById(oldId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // 1. HOÀN TÁC SỐ DƯ CỦA GIAO DỊCH CŨ (Trên Fund cũ)
        Fund oldFund = oldTx.getFund();
        if ("INCOME".equalsIgnoreCase(oldTx.getType())) {
            oldFund.setCurrentBalance(oldFund.getCurrentBalance() - oldTx.getAmount());
        } else {
            oldFund.setCurrentBalance(oldFund.getCurrentBalance() + oldTx.getAmount());
        }

        // 2. ĐÁNH DẤU BẢN GHI CŨ LÀ UPDATED
        oldTx.setStatus("UPDATED");
        transactionRepository.save(oldTx);

        // 3. TẠO BẢN GHI MỚI VÀ MAP DỮ LIỆU
        Transaction newTx = new Transaction();
        newTx.setParentId(oldId);
        newTx.setStatus("ACTIVE");

        // --- Bắt đầu set các trường từ Request ---

        // Lấy và gắn Nguồn tiền (Có thể là Fund cũ, hoặc Fund mới nếu người dùng chọn đổi Nguồn tiền)
        Fund targetFund = oldFund;
        if (!oldFund.getId().equals(newRequest.getFundId())) {
            targetFund = fundRepository.findById(newRequest.getFundId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Nguồn tiền (Fund) mới với ID: " + newRequest.getFundId()));
        }
        newTx.setFund(targetFund);

        // Lấy và gắn Category
        Category category = categoryRepository.findById(newRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục với ID: " + newRequest.getCategoryId()));
        newTx.setCategories(category);

        // Lấy và gắn User (Người thực hiện hành động update này)
        User user = userRepository.findById(newRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng với ID: " + newRequest.getUserId()));
        newTx.setUser(user);

        // Lấy và gắn Partner (Không bắt buộc)
        if (newRequest.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(newRequest.getPartnerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác với ID: " + newRequest.getPartnerId()));
            newTx.setPartner(partner);
        }

        // Map các trường cơ bản
        newTx.setType(newRequest.getType().toUpperCase());
        newTx.setAmount(newRequest.getAmount());
        newTx.setNote(newRequest.getNote());

        // Xử lý các trường thời gian và mã giao dịch
        newTx.setTransaction_code("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Lưu ý: Ngày phát sinh giao dịch nên lấy theo ngày của bản ghi gốc để không làm sai lệch báo cáo tháng
        newTx.setTransaction_date(oldTx.getTransaction_date());

        // Ngày giờ tạo bản ghi mới này thì lấy theo thời gian thực tại
        newTx.setCreated_at(new Date());
        newTx.setDatetime(new Date());

        // --- Kết thúc set các trường ---

        // 4. CẬP NHẬT SỐ DƯ THEO SỐ TIỀN MỚI (Trên targetFund)
        if ("INCOME".equalsIgnoreCase(newTx.getType())) {
            targetFund.setCurrentBalance(targetFund.getCurrentBalance() + newTx.getAmount());
        } else {
            // Validation: Nếu là phiếu chi, quỹ mới phải đủ tiền
            if (targetFund.getCurrentBalance() < newTx.getAmount()) {
                throw new RuntimeException("Số dư trong nguồn tiền không đủ để thực hiện Phiếu Chi cập nhật!");
            }
            targetFund.setCurrentBalance(targetFund.getCurrentBalance() - newTx.getAmount());
        }

        // 5. LƯU THAY ĐỔI VÀO DATABASE
        // Nếu đổi sang quỹ khác, phải lưu quỹ cũ (đã hoàn tiền) riêng
        if (!oldFund.getId().equals(targetFund.getId())) {
            fundRepository.save(oldFund);
        }
        fundRepository.save(targetFund); // Lưu quỹ mới (đã trừ/cộng tiền mới)

        return transactionRepository.save(newTx);
    }
    @Override
    @Transactional
    public void cancelTransaction(Long txId) {
        // 1. Lấy giao dịch an toàn
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với ID: " + txId));

        // 2. CHECK LOGIC: Chỉ cho phép hủy nếu giao dịch đang ở trạng thái ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Hủy thất bại: Chỉ có thể hủy giao dịch đang ở trạng thái ACTIVE!");
        }

        // 3. Đổi trạng thái thành CANCELLED
        tx.setStatus("CANCELLED");

        // 4. Hoàn lại tiền vào quỹ
        Fund fund = tx.getFund();
        if ("INCOME".equalsIgnoreCase(tx.getType())) {
            fund.setCurrentBalance(fund.getCurrentBalance() - tx.getAmount());
        } else {
            fund.setCurrentBalance(fund.getCurrentBalance() + tx.getAmount());
        }

        // 5. Lưu thay đổi
        fundRepository.save(fund);
        transactionRepository.save(tx);
    }
    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Giao dịch với ID: " + id));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        // Lấy tất cả giao dịch (nếu muốn chỉ lấy ACTIVE thì bạn sửa lại trong Repository nhé)
        return transactionRepository.findAll();
    }
    @Override
    public Double getTotalIncome() {
        try {
            // Gọi hàm tính tổng từ Repository và truyền loại là INCOME (Thu)
            Double totalIncome = transactionRepository.getTotalIncome();

            // Đảm bảo an toàn tuyệt đối (đề phòng database trả về null)
            if (totalIncome == null) {
                return 0.0;
            }
            return totalIncome;

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tính tổng thu: " + e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi tính tổng thu. Vui lòng thử lại sau!");
        }
    }

    @Override
    public Double getTotalExpense() {
        try {
            // Gọi hàm tính tổng từ Repository và truyền loại là EXPENSE (Chi)
            Double totalExpense = transactionRepository.getTotalExpense();

            // Đảm bảo an toàn tuyệt đối
            if (totalExpense == null) {
                return 0.0;
            }
            return totalExpense;

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tính tổng chi: " + e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi tính tổng chi. Vui lòng thử lại sau!");
        }
    }


}