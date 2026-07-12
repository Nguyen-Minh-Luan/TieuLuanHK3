package vn.edu.hcmuaf.fit.quanlythuchi.service.transfer;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.transfer.FundTransferRequest;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.time.LocalDateTime;

public interface FundTransferService {
    FundTransferDTO transferFund(FundTransferRequest request, User currentUser);
    
    Page<FundTransferDTO> getTransferHistory(Long fundId, LocalDateTime fromDate, LocalDateTime toDate, Long createdBy, int page, int size, String sortBy, String sortDir);
    
    FundTransferDTO getTransferById(Long id);
}
