package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.FundReconciliation;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundReconciliationRepository extends JpaRepository<FundReconciliation, Long> {

    @Query("SELECT r FROM FundReconciliation r WHERE " +
            "(:fundId IS NULL OR r.fund.id = :fundId) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:fromDate IS NULL OR r.periodStart >= :fromDate) AND " +
            "(:toDate IS NULL OR r.periodEnd <= :toDate)")
    Page<FundReconciliation> searchReconciliations(
            @Param("fundId") Long fundId,
            @Param("status") String status,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            Pageable pageable);

    // Kiểm tra xem có kỳ nào đã chốt (CLOSED) bao gồm ngày này không
    @Query("SELECT COUNT(r) > 0 FROM FundReconciliation r " +
            "WHERE r.fund.id = :fundId AND r.status = 'CLOSED' " +
            "AND :transactionDate BETWEEN r.periodStart AND r.periodEnd")
    boolean existsLockedPeriodForDate(@Param("fundId") Long fundId, @Param("transactionDate") Date transactionDate);

    // Lấy kỳ gần nhất đã chốt của một quỹ
    @Query("SELECT r FROM FundReconciliation r " +
            "WHERE r.fund.id = :fundId AND r.status = 'CLOSED' " +
            "ORDER BY r.periodEnd DESC LIMIT 1")
    Optional<FundReconciliation> findLatestClosedByFundId(@Param("fundId") Long fundId);

    // Tìm kiếm các kỳ bị chồng lấp
    @Query("SELECT COUNT(r) > 0 FROM FundReconciliation r " +
            "WHERE r.fund.id = :fundId AND r.status = 'CLOSED' " +
            "AND (r.periodStart <= :endDate AND r.periodEnd >= :startDate)")
    boolean existsOverlapClosedPeriod(@Param("fundId") Long fundId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Tìm kiếm các kỳ draft bị chồng lấp
    @Query("SELECT COUNT(r) > 0 FROM FundReconciliation r " +
            "WHERE r.fund.id = :fundId AND r.status = 'DRAFT' " +
            "AND (r.periodStart <= :endDate AND r.periodEnd >= :startDate)")
    boolean existsOverlapDraftPeriod(@Param("fundId") Long fundId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Lấy danh sách để check overlap
    @Query("SELECT r FROM FundReconciliation r " +
            "WHERE r.fund.id = :fundId AND r.status = 'CLOSED' " +
            "AND :transactionDate BETWEEN r.periodStart AND r.periodEnd")
    List<FundReconciliation> findLockedPeriodsForDate(@Param("fundId") Long fundId, @Param("transactionDate") Date transactionDate);
}
