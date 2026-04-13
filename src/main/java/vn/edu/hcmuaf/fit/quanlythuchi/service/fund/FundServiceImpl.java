package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundRepository;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService {
    private final FundRepository fundRepository;

    @Override
    public void deleteFund(Long id) {
        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguồn tiền để xóa với ID: " + id));
        if (!fund.getIsDeleted()) {
            fund.setIsDeleted(true);
            fundRepository.save(fund);
        }
    }

    @Override
    @Transactional
    public Fund createFund(FundResponseDTO fund) {
        Fund realFund = new Fund();
        realFund.setName(fund.getName());
        realFund.setType(fund.getType());
        realFund.setInitialBalance(fund.getInitialBalance());
        realFund.setCurrentBalance(fund.getInitialBalance());
        realFund.setStatus(fund.getStatus());
        realFund.setIsDeleted(false);
        realFund.setCreated_at(new Date());
        return fundRepository.save(realFund);
    }

    @Override
    public Fund updateFund(Long id, FundResponseDTO fundDTO) {
        try {
            return fundRepository.findById(id).map(existingFund -> {
                if(existingFund.getIsDeleted()){
                    throw new RuntimeException("Nguồn tiền này đã bị xoá không thể update");
                }
                if (fundDTO.getName() != null && !fundDTO.getName().trim().isEmpty()) {
                    existingFund.setName(fundDTO.getName());
                }
                if (fundDTO.getType() != null && !fundDTO.getType().trim().isEmpty()) {
                    existingFund.setType(fundDTO.getType());
                }
                if (fundDTO.getStatus() != null && !fundDTO.getStatus().trim().isEmpty()) {
                    existingFund.setStatus(fundDTO.getStatus());
                }
                if (fundDTO.getInitialBalance() != null) {
                    //bởi vì là initialBalance và currentBalance khác nhau nên ta sẽ cập nhật currentBalance sau khi chỉnh sửa initialBalance
                    // tính delta để tìm ra khoản tiền chênh lệch cho an toàn nhất
                    // tránh gán thẳng vào current vì làm vậy sẽ bị mất dữ liệu
                    Double oldInitialBalance = existingFund.getInitialBalance() != null ? existingFund.getInitialBalance() : 0.0;
                    Double newInitialBalance = fundDTO.getInitialBalance();
                    Double deltaBalance = newInitialBalance - oldInitialBalance;
                    Double currentBalance = existingFund.getCurrentBalance() != null ? existingFund.getCurrentBalance() : 0.0;
                    existingFund.setCurrentBalance(currentBalance + deltaBalance);
                    existingFund.setInitialBalance(newInitialBalance);
                }
                return fundRepository.save(existingFund);

            }).orElseThrow(() -> new RuntimeException("Không tìm thấy nguồn tiền với ID: " + id));

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi cập nhật: Tên nguồn tiền này có thể đã tồn tại!");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi cập nhật nguồn tiền: " + e.getMessage());
        }
    }

    public double increaseCurrentBalance(Fund fund, double amount) {
        return fund.getCurrentBalance() + amount;
    }

    public double decreaseCurrentBalance(Fund fund, double amount) {
        return fund.getCurrentBalance() - amount;
    }

    @Transactional
    public void updateCurrentBalance(Long fundId, Double amount, String transactionType) {
        fundRepository.findById(fundId).map(fund -> {
            Double currentBalance = fund.getCurrentBalance();
            if (currentBalance == null) {
                currentBalance = fund.getInitialBalance() != null ? fund.getInitialBalance() : 0.0;
            }

            if ("THU".equalsIgnoreCase(transactionType)) {
                fund.setCurrentBalance(currentBalance + amount);

            } else if ("CHI".equalsIgnoreCase(transactionType)) {
                if (currentBalance < amount) {
                    throw new RuntimeException("Số dư trong nguồn tiền không đủ để thực hiện phiếu chi này!");
                }
                fund.setCurrentBalance(currentBalance - amount);

            } else {
                throw new IllegalArgumentException("Loại phiếu không hợp lệ. Chỉ chấp nhận 'THU' hoặc 'CHI'");
            }
            return fundRepository.save(fund);
        }).orElseThrow(() -> new RuntimeException("Cập nhật thất bại: Không tìm thấy nguồn tiền với ID: " + fundId));
    }

    @Override
    public Optional<Fund> getFundById(Long id) {
        return fundRepository.findById(id);
    }
}
