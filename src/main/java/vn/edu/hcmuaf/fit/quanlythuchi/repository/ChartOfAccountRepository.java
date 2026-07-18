package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.AccountGroup;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.ChartOfAccount;

import java.util.List;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, String> {
    List<ChartOfAccount> findByGroup(AccountGroup group);
}
