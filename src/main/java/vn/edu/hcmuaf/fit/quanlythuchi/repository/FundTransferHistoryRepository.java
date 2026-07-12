package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundTransferHistory;

import java.time.LocalDateTime;

@Repository
public interface FundTransferHistoryRepository extends JpaRepository<FundTransferHistory, Long> {

    @Query("SELECT t FROM FundTransferHistory t WHERE " +
            "(:fundId IS NULL OR t.fromFund.id = :fundId OR t.toFund.id = :fundId) AND " +
            "(:fromDate IS NULL OR t.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR t.createdAt <= :toDate) AND " +
            "(:createdBy IS NULL OR t.createdBy.id = :createdBy)")
    Page<FundTransferHistory> searchTransfers(
            @Param("fundId") Long fundId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("createdBy") Long createdBy,
            Pageable pageable);
}
