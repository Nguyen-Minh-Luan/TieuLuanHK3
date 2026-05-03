package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Transaction;

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
}
