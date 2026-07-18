package vn.edu.hcmuaf.fit.quanlythuchi.service.fund;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundDTO;
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguồn tiền với ID: " + id));
        if (!fund.getIsDeleted()) {
            fund.setIsDeleted(true);
            fundRepository.save(fund);
        }
    }

    @Override
    @Transactional
    public FundDTO createFund(FundDTO request) {
        Fund fund = new Fund();
        fund.setName(request.getName());
        fund.setType(request.getType());
        fund.setInitialBalance(request.getInitialBalance());
        fund.setCurrentBalance(request.getInitialBalance()); // currentBalance = initialBalance lúc tạo
        fund.setStatus(request.getStatus());
        fund.setCode(request.getCode());
        fund.setNote(request.getNote());
        fund.setAccountCode(request.getAccountCode());
        fund.setIsDeleted(false);
        fund.setCreated_at(new Date());
        return toDTO(fundRepository.save(fund));
    }

    @Override
    public FundDTO updateFund(Long id, FundDTO request) {
        try {
            return fundRepository.findById(id).map(existingFund -> {
                if (existingFund.getIsDeleted()) {
                    throw new RuntimeException("Nguồn tiền này đã bị xoá, không thể cập nhật");
                }
                if (request.getName() != null && !request.getName().trim().isEmpty()) {
                    existingFund.setName(request.getName());
                }
                if (request.getType() != null && !request.getType().trim().isEmpty()) {
                    existingFund.setType(request.getType());
                }
                if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                    existingFund.setStatus(request.getStatus());
                }
                if (request.getInitialBalance() != null) {
                    // Tính delta để cập nhật currentBalance an toàn, không làm mất dữ liệu giao dịch
                    Double oldInitial = existingFund.getInitialBalance() != null ? existingFund.getInitialBalance() : 0.0;
                    Double delta = request.getInitialBalance() - oldInitial;
                    Double current = existingFund.getCurrentBalance() != null ? existingFund.getCurrentBalance() : 0.0;
                    existingFund.setCurrentBalance(current + delta);
                    existingFund.setInitialBalance(request.getInitialBalance());
                }
                if (request.getCode() != null) {
                    existingFund.setCode(request.getCode());
                }
                if (request.getNote() != null) {
                    existingFund.setNote(request.getNote());
                }
                if (request.getAccountCode() != null) {
                    existingFund.setAccountCode(request.getAccountCode());
                }
                return toDTO(fundRepository.save(existingFund));
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

    @Override
    @Transactional
    public void updateCurrentBalance(Long fundId, Double amount, String transactionType) {
        fundRepository.findById(fundId).map(fund -> {
            Double currentBalance = fund.getCurrentBalance() != null
                    ? fund.getCurrentBalance()
                    : (fund.getInitialBalance() != null ? fund.getInitialBalance() : 0.0);

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

    @Override
    public Double getTotalFund() {
        try {
            // Sử dụng .orElse(0.0) của Optional để cung cấp giá trị mặc định nếu bị null
            return fundRepository.getTotalFundBalance().orElse(0.0);
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi tính tổng số dư quỹ!");
        }
    }


    @Override
    @Transactional
    public Page<FundDTO> getAllFunds(String keyword, String type, String status,
                                    int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return fundRepository.searchFunds(keyword, type, status, pageable)
                             .map(this::toDTO);
    }

    private FundDTO toDTO(Fund fund) {
        return FundDTO.builder()
                .id(fund.getId())
                .name(fund.getName())
                .type(fund.getType())
                .status(fund.getStatus())
                .initialBalance(fund.getInitialBalance())
                .currentBalance(fund.getCurrentBalance())
                .code(fund.getCode())
                .note(fund.getNote())
                .accountCode(fund.getAccountCode())
                .updatedAt(fund.getUpdatedAt())
                .build();
    }
}
