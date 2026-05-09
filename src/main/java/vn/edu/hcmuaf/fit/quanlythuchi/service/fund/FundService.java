package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

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
}
