package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;

import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund,Long> {
    @Query("SELECT SUM(f.currentBalance) FROM Fund f WHERE f.isDeleted = false")
    Optional<Double> getTotalFundBalance();
}
