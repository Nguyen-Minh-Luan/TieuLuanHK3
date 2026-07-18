package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Tổng Thu
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t " +
            "JOIN t.categories c " +
            "WHERE c.type = vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType.INCOME")
    Double getTotalIncome();

    // Tổng Chi
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t " +
            "JOIN t.categories c " +
            "WHERE c.type = vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType.EXPENSE")
    Double getTotalExpense();
    /**
     * Lấy tổng chi tiêu theo từng tháng của một hạng mục trong N tháng gần nhất (không tính tháng hiện tại).
     * Trả về danh sách [year, month, totalAmount] để tính trung bình lịch sử.
     *
     * @param categoryId   ID của hạng mục
     * @param monthsBack   Số tháng lịch sử cần lấy (ví dụ: 3 hoặc 6)
     */
    @Query(value =
            "SELECT YEAR(t.transaction_date) AS yr, MONTH(t.transaction_date) AS mo, " +
                    "       COALESCE(SUM(t.amount), 0.0) AS total " +
                    "FROM transactions t " +
                    "WHERE t.categories_id = :categoryId " +
                    "  AND t.status = 'ACTIVE' " +
                    "  AND t.transaction_date < DATE_FORMAT(NOW(), '%Y-%m-01') " +
                    "  AND t.transaction_date >= DATE_FORMAT(NOW() - INTERVAL :monthsBack MONTH, '%Y-%m-01') " +
                    "GROUP BY YEAR(t.transaction_date), MONTH(t.transaction_date)",
            nativeQuery = true)
    List<Object[]> getMonthlyTotalByCategoryForLastNMonths(
            @Param("categoryId") Long categoryId,
            @Param("monthsBack") int monthsBack);

    /**
     * Lấy tổng chi tiêu của một hạng mục trong tháng hiện tại (chỉ giao dịch ACTIVE).
     */
    @Query(value =
            "SELECT COALESCE(SUM(t.amount), 0.0) " +
                    "FROM transactions t " +
                    "WHERE t.categories_id = :categoryId " +
                    "  AND t.status = 'ACTIVE' " +
                    "  AND YEAR(t.transaction_date) = YEAR(NOW()) " +
                    "  AND MONTH(t.transaction_date) = MONTH(NOW())",
            nativeQuery = true)
    Double getCurrentMonthTotalByCategory(@Param("categoryId") Long categoryId);

    /** Lấy tất cả phiếu thu/chi đã thanh toán cho một khoản nợ theo trạng thái */
    List<Transaction> findByDebt_IdAndStatus(Long debtId, String status);

    @Query("SELECT t FROM Transaction t WHERE t.status != 'CANCELLED'")
    Page<Transaction> findAllActive(Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN t.type IN ('INCOME','INCOME_DEBT') THEN t.amount " +
            "WHEN t.type IN ('EXPENSE','EXPENSE_DEBT') THEN -t.amount ELSE 0 END), 0) " +
            "FROM Transaction t WHERE t.fund.id = :fundId " +
            "AND t.status = 'ACTIVE' " +
            "AND t.transaction_date BETWEEN :from AND :to")
    Double sumNetAmountByFundAndPeriod(@Param("fundId") Long fundId, @Param("from") Date from, @Param("to") Date to);

    @Query("SELECT t FROM Transaction t " +
           "WHERE " +
           "(:keyword IS NULL OR " +
           "  LOWER(t.transaction_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(t.note)             LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type       IS NULL OR t.type            = :type) AND " +
           "(:status     IS NULL OR t.status          = :status) AND " +
           "(:fundId     IS NULL OR t.fund.id         = :fundId) AND " +
           "(:categoryId IS NULL OR t.categories.id   = :categoryId) AND " +
           "(:partnerId  IS NULL OR t.partner.id      = :partnerId) AND " +
           "(:userId     IS NULL OR t.user.id         = :userId) AND " +
           "(:fromDate   IS NULL OR t.transaction_date >= :fromDate) AND " +
           "(:toDate     IS NULL OR t.transaction_date <= :toDate)")
    Page<Transaction> searchTransactions(
        @Param("keyword")    String keyword,
        @Param("type")       String type,
        @Param("status")     String status,
        @Param("fundId")     Long fundId,
        @Param("categoryId") Long categoryId,
        @Param("partnerId")  Long partnerId,
        @Param("userId")     Long userId,
        @Param("fromDate")   java.util.Date fromDate,
        @Param("toDate")     java.util.Date toDate,
        Pageable pageable
    );

    /**
     * Lấy tổng thu/chi theo tháng trong N tháng gần nhất (bao gồm cả tháng hiện tại).
     * Trả về danh sách [year, month, totalIncome, totalExpense]
     */
    @Query(value =
            "SELECT YEAR(t.transaction_date) AS yr, MONTH(t.transaction_date) AS mo, " +
                    "       SUM(CASE WHEN t.type IN ('INCOME','INCOME_DEBT') THEN t.amount ELSE 0 END) AS totalIncome, " +
                    "       SUM(CASE WHEN t.type IN ('EXPENSE','EXPENSE_DEBT') THEN t.amount ELSE 0 END) AS totalExpense " +
                    "FROM transactions t " +
                    "WHERE t.status = 'ACTIVE' " +
                    "  AND t.transaction_date >= DATE_FORMAT(NOW() - INTERVAL :monthsBack MONTH, '%Y-%m-01') " +
                    "GROUP BY YEAR(t.transaction_date), MONTH(t.transaction_date) " +
                    "ORDER BY yr ASC, mo ASC",
            nativeQuery = true)
    List<Object[]> getMonthlyCashFlowForLastNMonths(@Param("monthsBack") int monthsBack);

    /**
     * Lấy tổng chi tiêu của TẤT CẢ các danh mục chi phí (EXPENSE) theo tháng trong N tháng gần nhất.
     * Trả về danh sách [categoryId, categoryName, year, month, totalAmount]
     */
    @Query(value =
            "SELECT t.categories_id, c.name, YEAR(t.transaction_date) AS yr, MONTH(t.transaction_date) AS mo, " +
                    "       COALESCE(SUM(t.amount), 0.0) AS total " +
                    "FROM transactions t " +
                    "JOIN categories c ON t.categories_id = c.id " +
                    "WHERE t.status = 'ACTIVE' " +
                    "  AND c.type = 'EXPENSE' " +
                    "  AND t.transaction_date >= DATE_FORMAT(NOW() - INTERVAL :monthsBack MONTH, '%Y-%m-01') " +
                    "GROUP BY t.categories_id, c.name, YEAR(t.transaction_date), MONTH(t.transaction_date) " +
                    "ORDER BY t.categories_id ASC, yr ASC, mo ASC",
            nativeQuery = true)
    List<Object[]> getMonthlyTotalExpenseAllCategoriesForLastNMonths(@Param("monthsBack") int monthsBack);
}
