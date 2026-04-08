package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;

import java.util.Optional;

@Service
public interface FundService {
    Fund createFund(Fund fund);
    Fund updateFund(Long id, Fund fundDetails);
    void deleteFund(Long id);
    Optional<Fund> getFundById(Long id);
}
