package vn.edu.hcmuaf.fit.quanlythuchi.service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ChartOfAccountDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.AccountGroup;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.ChartOfAccount;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.ChartOfAccountRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartOfAccountServiceImpl implements ChartOfAccountService {

    private final ChartOfAccountRepository repository;

    @Override
    public List<ChartOfAccountDTO> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartOfAccountDTO> getByGroup(String groupName) {
        try {
            AccountGroup group = AccountGroup.valueOf(groupName.toUpperCase());
            return repository.findByGroup(group).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private ChartOfAccountDTO mapToDTO(ChartOfAccount entity) {
        return ChartOfAccountDTO.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .group(entity.getGroup().name())
                .build();
    }
}
