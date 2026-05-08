package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

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
}
