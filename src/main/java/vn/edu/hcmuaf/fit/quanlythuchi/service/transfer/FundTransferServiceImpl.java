package vn.edu.hcmuaf.fit.quanlythuchi.service.transfer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundTransferHistory;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.FundTransferHistoryRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.fund.FundService;
import vn.edu.hcmuaf.fit.quanlythuchi.service.reconciliation.FundReconciliationService;
import vn.edu.hcmuaf.fit.quanlythuchi.util.MoneyToWordsConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    private final FundTransferHistoryRepository transferHistoryRepository;
    private final FundService fundService;
    private final FundReconciliationService reconciliationService;

    @Override
    @Transactional
    public FundTransferDTO transferFund(FundTransferRequest request, User currentUser) {
        if (request.getFromFundId().equals(request.getToFundId())) {
            throw new RuntimeException("Quỹ nguồn và quỹ đích phải khác nhau");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Số tiền chuyển phải lớn hơn 0");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập lý do chuyển tiền");
        }

        Fund fromFund = fundService.getFundById(request.getFromFundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ nguồn id = " + request.getFromFundId()));
        Fund toFund = fundService.getFundById(request.getToFundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ đích id = " + request.getToFundId()));

        if (fromFund.getIsDeleted() || !"ACTIVE".equals(fromFund.getStatus())) {
            throw new RuntimeException("Quỹ nguồn không khả dụng hoặc đã bị xóa");
        }
        if (toFund.getIsDeleted() || !"ACTIVE".equals(toFund.getStatus())) {
            throw new RuntimeException("Quỹ đích không khả dụng hoặc đã bị xóa");
        }
        
        Date now = new Date();
        // Kiểm tra khóa kỳ kiểm kê cho cả 2 quỹ
        reconciliationService.assertNotLocked(fromFund.getId(), now);
        reconciliationService.assertNotLocked(toFund.getId(), now);

        // fundService.updateCurrentBalance đã có kiểm tra số dư khi truyền "CHI"
        fundService.updateCurrentBalance(fromFund.getId(), request.getAmount(), "CHI");
        fundService.updateCurrentBalance(toFund.getId(), request.getAmount(), "THU");

        // Lấy lại số dư sau khi cập nhật để lưu lịch sử
        Double fromFundBalanceAfter = fundService.getFundById(fromFund.getId()).get().getCurrentBalance();
        Double toFundBalanceAfter = fundService.getFundById(toFund.getId()).get().getCurrentBalance();

        // Tạo mã phiếu
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String transferCode = "CQ-" + dateStr + "-" + shortUuid;

        FundTransferHistory history = new FundTransferHistory();
        history.setTransferCode(transferCode);
        history.setFromFund(fromFund);
        history.setToFund(toFund);
        history.setAmount(request.getAmount());
        history.setAmountInWord(MoneyToWordsConverter.convert(request.getAmount()));
        history.setReason(request.getReason());
        history.setNote(request.getNote());
        history.setStatus("SUCCESS");
        history.setCreatedBy(currentUser);
        history.setCreatedAt(LocalDateTime.now());
        history.setFromFundBalanceAfter(fromFundBalanceAfter);
        history.setToFundBalanceAfter(toFundBalanceAfter);

        return toDTO(transferHistoryRepository.save(history));
    }

    @Override
    public Page<FundTransferDTO> getTransferHistory(Long fundId, LocalDateTime fromDate, LocalDateTime toDate, Long createdBy, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return transferHistoryRepository.searchTransfers(fundId, fromDate, toDate, createdBy, pageable)
                .map(this::toDTO);
    }

    @Override
    public FundTransferDTO getTransferById(Long id) {
        FundTransferHistory history = transferHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử chuyển quỹ id = " + id));
        return toDTO(history);
    }

    private FundTransferDTO toDTO(FundTransferHistory history) {
        return FundTransferDTO.builder()
                .id(history.getId())
                .transferCode(history.getTransferCode())
                .fromFundId(history.getFromFund().getId())
                .fromFundName(history.getFromFund().getName())
                .toFundId(history.getToFund().getId())
                .toFundName(history.getToFund().getName())
                .amount(history.getAmount())
                .amountInWord(history.getAmountInWord())
                .reason(history.getReason())
                .note(history.getNote())
                .status(history.getStatus())
                .createdById(history.getCreatedBy().getId())
                .createdByName(history.getCreatedBy().getFullName())
                .createdAt(history.getCreatedAt())
                .fromFundBalanceAfter(history.getFromFundBalanceAfter())
                .toFundBalanceAfter(history.getToFundBalanceAfter())
                .build();
    }
}
