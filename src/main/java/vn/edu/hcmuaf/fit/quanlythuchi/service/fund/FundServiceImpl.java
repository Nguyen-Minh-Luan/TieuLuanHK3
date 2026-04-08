package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService{
    private final FundRepository fundRepository;
    @Override
    @Transactional
    public Fund createFund(Fund fund) {
        // Khi tạo mới, số dư hiện tại bằng số dư ban đầu
        if (fund.getCurrentBalance() == null) {
            fund.setCurrentBalance(fund.getInitialBalance());
        }
        return fundRepository.save(fund);
    }

    @Override
    public Fund updateFund(Long id, Fund fundDetails) {
        return fundRepository.findById(id).map(existingFund -> {
            existingFund.setName(fundDetails.getName());
            existingFund.setType(fundDetails.getType());
            existingFund.setStatus(fundDetails.getStatus());
            return fundRepository.save(existingFund);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy nguồn tiền với ID: " + id));
    }
    public double increaseCurrentBalance(Fund fund, double amount){
        return fund.getCurrentBalance() + amount;
    }
    public double decreaseCurrentBalance(Fund fund, double amount){
        return fund.getCurrentBalance() - amount;
    }

    @Override
    public void deleteFund(Long id) {

    }

    @Override
    public Optional<Fund> getFundById(Long id) {
        return Optional.empty();
    }
}
