package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Report;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /** Lấy tất cả báo cáo chưa bị xóa */
    List<Report> findByIsDeletedFalse();

    /** Lấy báo cáo theo ID chưa bị xóa */
    Optional<Report> findByIdAndIsDeletedFalse(Long id);

    /** Lấy báo cáo theo loại (MONTHLY, QUARTERLY...) */
    List<Report> findByTypeAndIsDeletedFalse(String type);

    /** Lấy báo cáo theo người tạo */
    List<Report> findByCreatedBy_IdAndIsDeletedFalse(Long userId);

    /**
     * Tính tổng thu cho một kỳ báo cáo từ bảng transactions.
     * Chỉ lấy giao dịch ACTIVE trong khoảng fromDate → toDate.
     */
    @Query(value =
            "SELECT COALESCE(SUM(t.amount), 0.0) " +
                    "FROM transactions t " +
                    "WHERE t.type = 'INCOME' " +
                    "  AND t.status = 'ACTIVE' " +
                    "  AND t.transaction_date BETWEEN :fromDate AND :toDate",
            nativeQuery = true)
    Double sumIncomeByDateRange(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate);

    /**
     * Tính tổng chi cho một kỳ báo cáo từ bảng transactions.
     */
    @Query(value =
            "SELECT COALESCE(SUM(t.amount), 0.0) " +
                    "FROM transactions t " +
                    "WHERE t.type = 'EXPENSE' " +
                    "  AND t.status = 'ACTIVE' " +
                    "  AND t.transaction_date BETWEEN :fromDate AND :toDate",
            nativeQuery = true)
    Double sumExpenseByDateRange(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate);

    /**
     * Lấy danh sách giao dịch ACTIVE trong khoảng fromDate – toDate.
     * Dùng nội bộ trong ReportServiceImpl để đính kèm vào ReportDTO.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.status = 'ACTIVE' " +
            "  AND t.transaction_date BETWEEN :fromDate AND :toDate " +
            "ORDER BY t.transaction_date ASC")
    List<Transaction> findTransactionsByDateRange(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate);
}