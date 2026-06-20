package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;

import java.util.Optional;

@Service
public interface FundService {
    FundDTO createFund(FundDTO request);
    FundDTO updateFund(Long id, FundDTO request);
    void deleteFund(Long id);
    void updateCurrentBalance(Long fundId, Double amount, String transactionType);
    Optional<Fund> getFundById(Long id);   // giữ Optional<Fund> vì TransactionServiceImpl dùng nội bộ
    Double getTotalFund();

    /**
     * Lấy danh sách quỹ tiền có phân trang và tìm kiếm.
     * @param keyword  Tìm theo name, type (LIKE %keyword%)
     * @param type     Lọc theo loại quỹ (CASH, BANK...)
     * @param status   Lọc theo trạng thái (ACTIVE, INACTIVE...)
     * @param page     Số trang (1-based)
     * @param size     Số phần tử mỗi trang
     * @param sortBy   Field sắp xếp (mặc định "name")
     * @param sortDir  Chiều sắp xếp: "asc" hoặc "desc"
     */
    Page<FundDTO> getAllFunds(String keyword, String type, String status,
                              int page, int size, String sortBy, String sortDir);
}
