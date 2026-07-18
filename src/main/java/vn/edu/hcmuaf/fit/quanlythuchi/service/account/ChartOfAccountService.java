package vn.edu.hcmuaf.fit.quanlythuchi.service.account;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.ChartOfAccountDTO;

import java.util.List;

public interface ChartOfAccountService {
    List<ChartOfAccountDTO> getAll();
    List<ChartOfAccountDTO> getByGroup(String groupName);
}
