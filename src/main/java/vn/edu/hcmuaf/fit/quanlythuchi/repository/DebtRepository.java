package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Debt;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    /** Lấy tất cả khoản nợ chưa bị xóa */
    List<Debt> findByIsDeletedFalse();

    /** Lấy khoản nợ theo ID, chưa bị xóa */
    Optional<Debt> findByIdAndIsDeletedFalse(Long id);

    /** Lấy danh sách nợ theo loại (RECEIVABLE hoặc PAYABLE) */
    List<Debt> findByDebtTypeAndIsDeletedFalse(String debtType);

    /** Lấy danh sách nợ theo đối tác */
    List<Debt> findByPartner_IdAndIsDeletedFalse(Long partnerId);

    /** Lấy danh sách nợ chưa thanh toán xong */
    List<Debt> findByIsPaidFalseAndIsDeletedFalse();

    /** Lấy danh sách nợ chưa thanh toán xong theo loại */
    List<Debt> findByDebtTypeAndIsPaidFalseAndIsDeletedFalse(String debtType);

    /** Lấy danh sách nợ chưa trả, có ngày đến hạn, sắp xếp tăng dần theo ngày đến hạn (dùng để cảnh báo) */
    List<Debt> findTop5ByDebtTypeAndIsPaidFalseAndIsDeletedFalseAndDueDateNotNullOrderByDueDateAsc(String debtType);

    /**
     * Tính tổng số tiền nợ còn phải thu (RECEIVABLE, chưa trả xong).
     * Dùng cho dashboard tổng quan.
     */
    @Query("SELECT COALESCE(SUM(d.totalAmount - d.paidAmount), 0.0) FROM Debt d " +
           "WHERE d.debtType = 'RECEIVABLE' AND d.isPaid = false AND d.isDeleted = false")
    Double getTotalRemainingReceivable();

    /**
     * Tính tổng số tiền nợ còn phải trả (PAYABLE, chưa trả xong).
     */
    @Query("SELECT COALESCE(SUM(d.totalAmount - d.paidAmount), 0.0) FROM Debt d " +
           "WHERE d.debtType = 'PAYABLE' AND d.isPaid = false AND d.isDeleted = false")
    Double getTotalRemainingPayable();

    /**
     * Tìm kiếm khoản nợ có phân trang, hỗ trợ keyword và bộ lọc động
     */
    @Query("SELECT d FROM Debt d " +
           "WHERE d.isDeleted = false AND " +
           "(:keyword IS NULL OR " +
           "  LOWER(d.debtType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(d.note)     LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(d.partner.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:debtType IS NULL OR d.debtType = :debtType) AND " +
           "(:isPaid   IS NULL OR d.isPaid   = :isPaid)")
    Page<Debt> searchDebts(
            @Param("keyword")  String keyword,
            @Param("debtType") String debtType,
            @Param("isPaid")   Boolean isPaid,
            Pageable pageable
    );
}
