package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;

import java.util.Optional;

@Service
public interface FundService {
    Fund createFund(FundResponseDTO fund);
    Fund updateFund(Long id, FundResponseDTO fundDTO);
    void deleteFund(Long id);
    void updateCurrentBalance(Long fundId, Double amount, String transactionType);
    Optional<Fund> getFundById(Long id);
}
